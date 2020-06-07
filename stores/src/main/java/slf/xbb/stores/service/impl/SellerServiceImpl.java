package slf.xbb.stores.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.mapper.SellerMapper;
import slf.xbb.stores.mapper.ShopMapper;
import slf.xbb.stores.service.ISellerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商户 服务实现类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
@Service
public class SellerServiceImpl extends ServiceImpl<SellerMapper, Seller> implements ISellerService {

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    @Transactional
    public Seller create(Seller seller) {
        LocalDateTime date = LocalDateTime.now();
        seller.setCreatedAt(date);
        seller.setUpdatedAt(date);
        seller.setRemarkScore(new BigDecimal(0));
        seller.setDisabledFlag(0);
        sellerMapper.insert(seller);
        return getById(seller.getId());
    }

    @Override
    public Seller changeStatus(Integer id, Integer diableFlag) throws BussinessException {
        Seller seller = getById(id);
        if (seller==null){
            throw new BussinessException(EmBusinessError.NO_OBJECT_FOUND);
        }
        seller.setDisabledFlag(diableFlag);
        updateById(seller);
        return seller;
    }

    @Override
    public IPage<Seller> selectSellerPage(Page<Seller> page, Integer state) {
        // 不进行 count sql 优化，解决 MP 无法自动优化 SQL 问题，这时候你需要自己查询 count 部分
        // page.setOptimizeCountSql(false);
        // 当 total 为小于 0 或者设置 setSearchCount(false) 分页插件不会进行 count 查询
        // 要点!! 分页返回的对象与传入的对象是同一个
        // sellerMapper.selectP
        QueryWrapper<Seller> queryWrapper =new QueryWrapper<>();
        queryWrapper.le("remark_score", 4);
        return sellerMapper.selectPageVo(page, queryWrapper);
    }


    IPage<Seller> selectSellerPage() {
        return null;
    }
}
