package slf.xbb.stores.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商户
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Seller对象", description="商户")
public class Seller implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal remarkScore;

    private Integer disabledFlag;


}
