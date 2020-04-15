package slf.xbb.stores.common;

import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by hzllb on 2019/7/9.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse, Exception ex) {
        if (ex instanceof BussinessException) {
            return CommonReturnType.create(((BussinessException) ex).getCommonError(), "fail");
        } else if (ex instanceof NoHandlerFoundException) {
            CommonError commonError = EmBusinessError.NO_HANDLER_FOUND;
            return CommonReturnType.create(commonError, "fail");
        } else if (ex instanceof ServletRequestBindingException) {
            CommonError commonError = EmBusinessError.BIND_EXCEPTION_ERROR;
            return CommonReturnType.create(commonError, "fail");
        } else {
            CommonError commonError = EmBusinessError.UNKNOWN_ERROR;
            return CommonReturnType.create(commonError, "fail");
        }

    }
}
