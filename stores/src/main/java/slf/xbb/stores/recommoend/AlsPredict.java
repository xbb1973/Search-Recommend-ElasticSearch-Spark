package slf.xbb.stores.recommoend;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author ：xbb
 * @date ：Created in 2020/6/23 9:40 上午
 * @description：ALS预测
 * @modifiedBy：
 * @version:
 */
public class AlsPredict {

    public static void main(String[] args) throws IOException {

        // 1、初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("shopApp").getOrCreate();

        // 2、csv数据读入为JavaRDD
        //加载模型进内存
        ALSModel alsModel = ALSModel.load("file:///Users/xbb1973/Documents/code/workdir/Search-Recommend-ElasticSearch-Spark/stores/data/alsmodel");


        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/xbb1973/Documents/code/workdir/Search-Recommend-ElasticSearch-Spark/stores/data/behavior.csv").toJavaRDD();

        JavaRDD<RatingBehavior> ratingJavaRDD = csvFile.map(new Function<String, RatingBehavior>() {
            @Override
            public RatingBehavior call(String v1) throws Exception {
                return RatingBehavior.parseRatingBehavior(v1);
            }
        });

        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD, RatingBehavior.class);
        //给5个用户做离线的召回结果预测
        Dataset<Row> users = rating.select(alsModel.getUserCol()).distinct().limit(5);
        Dataset<Row> userRecs = alsModel.recommendForUserSubset(users, 20);

        userRecs.foreachPartition(new ForeachPartitionFunction<Row>() {
            @Override
            public void call(Iterator<Row> t) throws Exception {

                //新建数据库链接
                Connection connection = DriverManager.
                        getConnection("jdbc:mysql:///stores?" +
                                "user=root&password=123&useUnicode=true&characterEncoding=UTF-8");
                PreparedStatement preparedStatement = connection.
                        prepareStatement("insert into recommend(id,recommend)values(?,?)");

                List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

                t.forEachRemaining(action -> {
                    int userId = action.getInt(0);
                    List<GenericRowWithSchema> recommendationList = action.getList(1);
                    List<Integer> shopIdList = new ArrayList<Integer>();
                    recommendationList.forEach(row -> {
                        Integer shopId = row.getInt(0);
                        shopIdList.add(shopId);
                    });
                    String recommendData = StringUtils.join(shopIdList, ",");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("userId", userId);
                    map.put("recommend", recommendData);
                    data.add(map);
                });

                data.forEach(stringObjectMap -> {
                    try {
                        preparedStatement.setInt(1, (Integer) stringObjectMap.get("userId"));
                        preparedStatement.setString(2, (String) stringObjectMap.get("recommend"));

                        preparedStatement.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                });
                preparedStatement.executeBatch();
                connection.close();
            }
        });
    }

    @Data
    public static class RatingBehavior implements Serializable {
        private int userId;
        private int shopId;
        private int rating;

        public RatingBehavior(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }

        public static RatingBehavior parseRatingBehavior(String str) {
            str = str.replace("\"", "");
            String[] split = str.split(",");
            int userId = Integer.parseInt(split[0]);
            int shopId = Integer.parseInt(split[1]);
            int rating = Integer.parseInt(split[2]);
            return new RatingBehavior(userId, shopId, rating);
        }
    }

}
