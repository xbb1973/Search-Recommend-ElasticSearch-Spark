package slf.xbb.stores.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 门店
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Shop对象", description="门店")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * MBP自动填充
     * 是对所有表中有这两个字段，并且在实体类中的属性中设置了以下注解的表生效。
     * @TableField(fill = FieldFill.INSERT)
     * //insert语句生效 或者
     * @TableField(fill = FieldFill.UPDATE)
     * //update语句生效 或者
     * @TableField(fill = FieldFill.INSERT_UPDATE)
     * //insert和update语句都生效
     * 还有你需要调用MP提供给你的操作实体的方法，例如insert,updateById等，不传实体的方法是不生效的。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
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


}
