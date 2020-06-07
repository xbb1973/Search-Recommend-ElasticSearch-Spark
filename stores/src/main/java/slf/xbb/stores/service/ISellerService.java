package slf.xbb.stores.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.entity.Seller;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商户 服务类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface ISellerService extends IService<Seller> {
    // Seller getSeller(Integer id);
    // List<Seller> listSellers();
    Seller create(Seller seller);
    Seller changeStatus(Integer id, Integer diableFlag) throws BussinessException;
    IPage<Seller> selectSellerPage(Page<Seller> page, Integer state);
}
