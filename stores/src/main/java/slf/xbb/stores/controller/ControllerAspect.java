package slf.xbb.stores.controller;

import com.alibaba.druid.util.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import slf.xbb.stores.common.AdminPermission;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.CommonReturnType;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.controller.admin.AdminController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/30 2:41 下午
 * @description：控制器切面
 * @modifiedBy：
 * @version:
 */
@Aspect
@Configuration
public class ControllerAspect {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse httpServletResponse;

    // @Around("execution(* com.imooc.dianping.controller.admin.*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    @Around("execution(* slf.xbb.stores.controller.admin.*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object adminControllerBeforeValid(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        AdminPermission adminPermission = method.getAnnotation(AdminPermission.class);
        if (adminPermission==null){
            // 公共方法
            Object reusltObj = proceedingJoinPoint.proceed();
            return reusltObj;
        }
        // 判断当前管理员是否登陆
        String email = (String) httpServletRequest.getSession().getAttribute(AdminController.CURRENT_ADMIN_SESSION);
        if (StringUtils.isEmpty(email)){
            if (adminPermission.produceType().equals("text/html")) {
                httpServletResponse.sendRedirect("/stores/admin/loginPage");
                return null;
            } else {
                // throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
                return CommonReturnType.create(EmBusinessError.USER_NOT_LOGIN, "fail");
            }
        } else {
            // httpServletResponse.sendRedirect("/stores/admin/index");
            Object reusltObj = proceedingJoinPoint.proceed();
            return reusltObj;
        }

    }
}
