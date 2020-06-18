package slf.xbb.stores.service;

import com.github.pagehelper.PageInfo;
import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;
import slf.xbb.stores.vo.PageQuery;
import slf.xbb.stores.vo.RecommendReq;
import slf.xbb.stores.vo.SearchReq;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 门店 服务类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface IShopService extends IService<Shop> {
    ShopBo create(Shop shop) throws BussinessException;
    ShopBo get(Integer id);
    List<ShopBo> getShopList();
    PageInfo<ShopBo> getPage(PageQuery pageQuery);
    List<ShopBo> recommend(RecommendReq recommendReq) throws BussinessException;
    List<ShopBo> search(SearchReq searchReq) throws BussinessException;
    List<Map<String, Object>> searchGroupByTags(SearchReq searchReq) throws BussinessException;
    List<Map<String, Object>> searchGroupByTags(List<ShopBo> shopBoList) throws BussinessException;
    List<ShopBo> searchES(SearchReq searchReq) throws BussinessException, IOException;
}
