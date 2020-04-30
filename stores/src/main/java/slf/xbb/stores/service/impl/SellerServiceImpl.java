package slf.xbb.stores.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.mapper.SellerMapper;
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
}
