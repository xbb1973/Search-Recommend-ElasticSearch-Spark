package slf.xbb.stores.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.entity.Shop;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import slf.xbb.stores.vo.RecommendReq;
import slf.xbb.stores.vo.SearchReq;
import slf.xbb.stores.vo.ShopCreateReq;

import java.util.List;
import java.util.Map;

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
    List<Map<String, Object>> searchGroupByTags(SearchReq searchReq);

    /**
     * <p>
     * 查询 : 根据state状态查询用户列表，分页显示
     * </p>
     *
     * @param page 分页对象,xml中可以从里面进行取值,传递参数 Page 即自动分页,必须放在第一位(你可以继承Page实现自己的分页对象)
     * @param state 状态
     * @return 分页对象
     */
    IPage<Seller> selectPageVo(Page<?> page, Integer state);
}
