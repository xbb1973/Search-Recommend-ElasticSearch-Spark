package slf.xbb.stores.bo;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.entity.Seller;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/1 1:56 下午
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class ShopBo {

    private Integer id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String name;

    private BigDecimal remarkScore;

    private Integer pricePerMan;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer categoryId;

    private String tags;

    private String startTime;

    private String endTime;

    private String address;

    private Integer sellerId;

    private String iconUrl;

    // 门店与类目/商家是多对一的关系
    private Category category;
    private Seller seller;

    @Min(value = 0, message = "最小距离不可小于0")
    private Integer distance;
}
