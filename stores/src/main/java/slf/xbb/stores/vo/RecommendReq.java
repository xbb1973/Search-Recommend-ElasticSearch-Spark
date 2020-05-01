package slf.xbb.stores.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/1 5:56 下午
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class RecommendReq {

    @NotNull(message = "纬度不许为空")
    private BigDecimal latitude;

    @NotNull(message = "经度不许为空")
    private BigDecimal longitude;

}
