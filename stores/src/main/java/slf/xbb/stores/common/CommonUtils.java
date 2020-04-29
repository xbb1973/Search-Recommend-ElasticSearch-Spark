package slf.xbb.stores.common;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/29 4:12 下午
 * @description：公共方法
 * @modifiedBy：
 * @version:
 */
public class CommonUtils {

    public static String processErrorString(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            // 使用,逗号拼接
            stringBuilder.append(fieldError.getDefaultMessage()+",");
        }
        // 将最后一个,逗号去掉
        stringBuilder.subSequence(0, stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
