package slf.xbb.stores.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import slf.xbb.stores.entity.Seller;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 商户 Mapper 接口
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface SellerMapper extends BaseMapper<Seller> {
    /**
     * <p>
     * 查询 : 根据state状态查询用户列表，分页显示
     * </p>
     *
     * @param page 分页对象,xml中可以从里面进行取值,传递参数 Page 即自动分页,必须放在第一位(你可以继承Page实现自己的分页对象)
     * @param state 状态
     * @return 分页对象
     */
    IPage<Seller> selectPageVo(Page<?> page, @Param(Constants.WRAPPER) Wrapper<Seller> wrapper);
}
