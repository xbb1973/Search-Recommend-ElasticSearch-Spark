package slf.xbb.stores.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/29 2:58 下午
 * @description：注册请求vo
 * @modifiedBy：
 * @version:
 */
@Data
public class RegistryReq {

    @NotBlank(message = "手机号码不许为空")
    private String telphone;

    @NotBlank(message = "密码不许为空")
    private String password;

    @NotBlank(message = "昵称不许为空")
    private String nickName;

    @NotNull(message = "性别不许为空")
    private Integer gender;

}
