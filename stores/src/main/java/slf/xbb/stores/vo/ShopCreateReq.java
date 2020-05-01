package slf.xbb.stores.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/1 1:14 下午
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class ShopCreateReq {

    @NotBlank(message = "门店名不许为空")
    private String name;

    // @NotBlank(message = "门店名不许为空")
    private BigDecimal remarkScore;

    @NotNull(message = "人均价格不许为空")
    @Min(value = 0, message = "价格不允许低于0")
    private Integer pricePerMan;

    @NotNull(message = "纬度不许为空")
    private BigDecimal latitude;

    @NotNull(message = "经度不许为空")
    private BigDecimal longitude;

    @NotNull(message = "类目不许为空")
    private Integer categoryId;

    private String tags;

    @NotBlank(message = "开始营业时间不许为空")
    private String startTime;

    @NotBlank(message = "结束营业不许为空")
    private String endTime;

    @NotBlank(message = "地址不许为空")
    private String address;

    @NotNull(message = "商家不许为空")
    private Integer sellerId;

    @NotBlank(message = "iconUrl不许为空")
    private String iconUrl;
}
