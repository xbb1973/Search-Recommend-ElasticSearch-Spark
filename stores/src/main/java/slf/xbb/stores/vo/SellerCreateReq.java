package slf.xbb.stores.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/30 9:58 下午
 * @description：Seller创建的VO
 * @modifiedBy：
 * @version:
 */
@Data
public class SellerCreateReq {

    @NotBlank(message = "商户名称不许为空")
    private String name;

}
