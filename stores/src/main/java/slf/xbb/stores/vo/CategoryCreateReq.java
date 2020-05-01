package slf.xbb.stores.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/1 7:40 上午
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class CategoryCreateReq {

    @NotBlank(message = "品类名不许为空")
    private String name;

    @NotBlank(message = "品类iconUrl不许为空")
    private String iconUrl;

    @NotNull(message = "品类排序权重不许为空")
    private Integer sort;

}
