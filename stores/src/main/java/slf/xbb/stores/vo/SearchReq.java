package slf.xbb.stores.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/2 7:41 上午
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class SearchReq {

    @NotNull(message = "纬度不许为空")
    private BigDecimal latitude;

    @NotNull(message = "经度不许为空")
    private BigDecimal longitude;

    @NotBlank(message = "关键字不许为空")
    private String keyword;

    private Integer orderBy;

    private Integer categoryId;

    private String tags;
}
