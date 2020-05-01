package slf.xbb.stores.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/30 2:40 下午
 * @description：统一鉴权切面
 * @modifiedBy：
 * @version:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminPermission {
    // "application/json"
    String produceType() default "text/html";
}
