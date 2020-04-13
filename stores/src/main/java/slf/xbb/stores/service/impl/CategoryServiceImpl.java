package slf.xbb.stores.service.impl;

import slf.xbb.stores.entity.Category;
import slf.xbb.stores.mapper.CategoryMapper;
import slf.xbb.stores.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商家 服务实现类
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
