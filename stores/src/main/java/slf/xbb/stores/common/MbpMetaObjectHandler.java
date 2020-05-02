package slf.xbb.stores.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/2 5:32 下午
 * @description：MBP自动填充定制实现
 * @modifiedBy：
 * @version:
 */
@Component
public class MbpMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // name 是实体类中的名称而不是数据库中列名
        System.out.println("insert Fill");
        // 优化自动填充，只有需要的才填充
        // 判断代码里有无赋值，没有则填充
        Object createdAt = getFieldValByName("createdAt", metaObject);
        Object updatedAt = getFieldValByName("updatedAt", metaObject);
        // 判断属性值是否有set方法，有才填充
        if (metaObject.hasSetter("createdAt") && createdAt == null) {
            setFieldValByName("createdAt", LocalDateTime.now(), metaObject);
        }
        if (metaObject.hasSetter("updatedAt") && updatedAt == null) {
            setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
        }

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter("updatedAt")) {
            setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
        }
    }
}
