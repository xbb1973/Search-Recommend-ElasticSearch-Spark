package slf.xbb.stores.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商家
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Category对象", description="商家")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String name;

    private String iconUrl;

    private Integer sort;


}
