package slf.xbb.stores.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.CommonUtils;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.entity.Shop;
import slf.xbb.stores.mapper.ShopMapper;
import slf.xbb.stores.service.ICategoryService;
import slf.xbb.stores.service.ISellerService;
import slf.xbb.stores.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import slf.xbb.stores.vo.PageQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * 门店 服务实现类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    public static int SellerIsDiasbled = 1;
    public static int SellerNotDiasbled = 0;

    @Autowired
    ShopMapper shopMapper;

    @Autowired
    ISellerService sellerService;

    @Autowired
    ICategoryService categoryService;

    @Override
    @Transactional
    public ShopBo create(Shop shop) throws BussinessException {
        LocalDateTime localDateTime = LocalDateTime.now();
        shop.setCreatedAt(localDateTime);
        shop.setUpdatedAt(localDateTime);
        Seller seller = sellerService.getById(shop.getSellerId());
        if (seller == null) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商户不存在");
        }
        if (seller.getDisabledFlag().intValue() == SellerIsDiasbled) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商户被禁用");
        }
        Category category = categoryService.getById(shop.getCategoryId());
        if (category == null) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "无此类目");
        }
        shopMapper.insert(shop);
        return get(shop.getId());
    }

    @Override
    public ShopBo get(Integer id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null){
            return null;
        }
        ShopBo shopBo = convertShopDoToShopBo(shop);
        return shopBo;
    }

    @Override
    public PageInfo<ShopBo> getPage(PageQuery pageQuery) {
        PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize());
        List<Shop> shopList = list();
        PageInfo<Shop> shopPageInfo = new PageInfo(shopList);
        PageInfo<ShopBo> shopBoPageInfo = CommonUtils.pageInfo2PageInfoDTO(shopPageInfo, ShopBo.class);
        shopBoPageInfo.getList().forEach(shopBo -> {
            shopBo.setCategory(categoryService.getById(shopBo.getCategoryId()));
            shopBo.setSeller(sellerService.getById(shopBo.getSellerId()));
        });
        return shopBoPageInfo;
    }

    @Override
    public List<ShopBo> getShopList() {

        List<Shop> shopList = list();
        List<ShopBo> shopBoList = convertShopDoListToShopBoList(shopList);
        return shopBoList;
    }

    private ShopBo convertShopDoToShopBo(Shop shop) {
        if (shop == null) {
            return null;
        }
        ShopBo shopBo = new ShopBo();
        BeanUtils.copyProperties(shop, shopBo);
        shopBo.setCategory(categoryService.getById(shopBo.getCategoryId()));
        shopBo.setSeller(sellerService.getById(shopBo.getSellerId()));
        return shopBo;
    }

    private List<ShopBo> convertShopDoListToShopBoList(List<Shop> shopList) {
        if (shopList == null) {
            return null;
        }
        List<ShopBo> shopBoList = new CopyOnWriteArrayList<>();
        shopList.forEach(shop -> {
            ShopBo shopBo = convertShopDoToShopBo(shop);
            shopBoList.add(shopBo);
        });
        return shopBoList;
    }
}
