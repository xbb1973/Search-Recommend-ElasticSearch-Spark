package slf.xbb.stores.mapper;

import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.entity.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import slf.xbb.stores.vo.RecommendReq;
import slf.xbb.stores.vo.SearchReq;
import slf.xbb.stores.vo.ShopCreateReq;

import java.util.List;

/**
 * <p>
 * 门店 Mapper 接口
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface ShopMapper extends BaseMapper<Shop> {

    List<ShopBo> recommend(RecommendReq recommendReq);
    List<ShopBo> search(SearchReq searchReq);
}
