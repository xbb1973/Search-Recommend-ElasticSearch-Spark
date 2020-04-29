package slf.xbb.stores.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/29 2:59 下午
 * @description：登陆请求vo
 * @modifiedBy：
 * @version:
 */
@Data
public class LoginReq {

    @NotBlank(message = "手机号码不许为空")
    private String telphone;

    @NotBlank(message = "密码不许为空")
    private String password;

}
