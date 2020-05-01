package slf.xbb.stores.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.mapper.CategoryMapper;
import slf.xbb.stores.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 商家 服务实现类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Category create(Category category) throws BussinessException {
        LocalDateTime dateTime = LocalDateTime.now();
        category.setCreatedAt(dateTime);
        category.setUpdatedAt(dateTime);
        try {
            categoryMapper.insert(category);
        } catch (Exception e) {
            throw new BussinessException(EmBusinessError.CATEGORY_DUP_ERROR);
        } finally {
        }
        return getById(category.getId());
    }
}
