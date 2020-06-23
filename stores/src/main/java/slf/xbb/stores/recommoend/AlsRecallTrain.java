package slf.xbb.stores.recommoend;

import lombok.Data;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author ：xbb
 * @date ：Created in 2020/6/22 5:41 下午
 * @description：ALS召回模型训练
 * @modifiedBy：
 * @version:
 */
public class AlsRecallTrain implements Serializable {

    public static void main(String[] args) throws IOException {

        // 1、初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("shopApp").getOrCreate();

        // 2、csv数据读入为JavaRDD
        //      JavaRDD数据类型转换
        //      JavaRDD只是定义了数据结构，并没有将数据实体xxx
        JavaRDD<String> csvFile = spark.read().textFile("file:///Users/xbb1973/Documents/code/workdir/Search-Recommend-ElasticSearch-Spark/stores/data/behavior.csv").toJavaRDD();
        // JavaRDD<String> csvFile = spark.read().textFile("file:///Users/xbb1973/Downloads/devtool/data/behavior.csv").toJavaRDD();

        JavaRDD<RatingBehavior> ratingJavaRDD = csvFile.map(new Function<String, RatingBehavior>() {
            @Override
            public RatingBehavior call(String v1) throws Exception {
                return RatingBehavior.parseRatingBehavior(v1);
            }
        });

        // 3、JavaRDD转化为Datase数据实体
        //      训练数据集划分
        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD, RatingBehavior.class);

        // 将所有的rating数据分成82份
        Dataset<Row>[] splits = rating.randomSplit(new double[]{0.8, 0.2});

        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testingData = splits[1];

        //过拟合：增大数据规模，减少RANK,增大正则化的系数
        //欠拟合：增加rank，减少正则化系数
        ALS als = new ALS().setMaxIter(10).setRank(5).setRegParam(0.01).
                setUserCol("userId").setItemCol("shopId").setRatingCol("rating");

        //模型训练
        ALSModel alsModel = als.fit(trainingData);

        //模型评测
        Dataset<Row> predictions = alsModel.transform(testingData);

        //rmse 均方根误差，预测值与真实值的偏差的平方除以观测次数，开个根号
        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse")
                .setLabelCol("rating").setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        System.out.println("rmse = " + rmse);
        alsModel.write().overwrite().save("file:///Users/xbb1973/Documents/code/workdir/Search-Recommend-ElasticSearch-Spark/stores/data/alsmodel");
        // alsModel.save("file:///Users/xbb1973/Downloads/devtool/data/alsmodel");

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
