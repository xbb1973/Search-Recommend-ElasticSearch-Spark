package slf.xbb.stores.vo;

import lombok.Data;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/30 11:40 下午
 * @description：分页查询请求
 * @modifiedBy：
 * @version:
 */
@Data
public class PageQuery {
    private Integer page = 1;
    private Integer size = 20;
}
