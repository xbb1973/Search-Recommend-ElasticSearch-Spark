package slf.xbb.stores.service;

import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商家 服务类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface ICategoryService extends IService<Category> {
    Category create(Category category) throws BussinessException;

}
