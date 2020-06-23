package slf.xbb.stores.recommoend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slf.xbb.stores.entity.Recommend;
import slf.xbb.stores.mapper.RecommendMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：xbb
 * @date ：Created in 2020/6/23 9:57 上午
 * @description：基于Spark机器学习的推荐
 * @modifiedBy：
 * @version:
 */
@Service
public class RecommendService {

    @Autowired
    private RecommendMapper recommendMapper;

    //召回数据，根据userid 召回shopidList
    public List<Integer> recall(Integer userId) {

        Recommend recommend = recommendMapper.selectById(userId);
        if (recommend == null) {
            recommend = recommendMapper.selectById(9999999);
        }
        String[] shopIdArr = recommend.getRecommend().split(",");
        List<Integer> shopIdList = new ArrayList<>();
        for (int i = 0; i < shopIdArr.length; i++) {
            shopIdList.add(Integer.valueOf(shopIdArr[i]));
        }
        return shopIdList;
    }

}
