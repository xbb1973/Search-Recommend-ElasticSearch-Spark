package slf.xbb.stores.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import slf.xbb.stores.vo.RecommendReq;
import slf.xbb.stores.vo.SearchReq;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
        if (shop == null) {
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

    @Override
    public List<ShopBo> recommend(RecommendReq recommendReq) throws BussinessException {
        // 实现1、将逻辑放在service中，取出list后进行排序判断等
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        //
        // ,ceil(1 + 1000*(2 * 6378.137* ASIN(SQRT(POW(SIN(PI() * (#{latitude} - latitude) / 360), 2) + COS(PI() * #{latitude} / 180)
        // * COS(latitude* PI() / 180) * POW(SIN(PI() * (#{longitude} - longitude) / 360), 2))))) AS distance
        //     from shop order by (0.95*1/log10(distance)+ 0.05*remark_score/5)  DESC
        //
        queryWrapper.orderByDesc("id");
        List<Shop> shopList = shopMapper.selectList(queryWrapper);

        if (shopList == null) {
            throw new BussinessException(EmBusinessError.NO_OBJECT_FOUND, "无合适推荐门店");
        }
        List<ShopBo> shopBoList = convertShopDoListToShopBoList(shopList);
        shopBoList.forEach(shopBo -> {
            double distance = CommonUtils.getDistance(shopBo.getLongitude().doubleValue(), shopBo.getLatitude().doubleValue(), recommendReq.getLongitude().doubleValue(), recommendReq.getLatitude().doubleValue());
            shopBo.setDistance((int) distance);
        });
        shopBoList.forEach(shopBo -> {
            System.out.println(shopBo.getDistance());
        });
        //
        // Collections.sort(list, new Comparator<StuVO>() {
        //     @Override
        //     public int compare(StuVO o1, StuVO o2) {
        //         int i = o1.getScore() - o2.getScore();
        //         return i;
        //     }
        // });

        // 实现2、直接在mapper中进行排序
        // List<ShopBo> shopBoList = shopMapper.recommend(recommendReq);


        return shopBoList;
    }

    /**
     * 搜索算法实现：
     * 1、mapper.xml定义数据库检索、排序
     * 2、service中查检索数据后再进行排序
     *
     * @param searchReq
     * @return
     * @throws BussinessException
     */
    @Override
    public List<ShopBo> search(SearchReq searchReq) throws BussinessException {
        // 实现1、将排序逻辑放在service中，检索逻辑使用MBP QueryWrapper实现
        // 1、检索数据库，取出name、category、tags符合条件的内容
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        // from shop order by (0.95*1/log10(distance)+ 0.05*remark_score/5)  DESC
        queryWrapper.like("name", searchReq.getKeyword());
        if (searchReq.getCategoryId() != null) {
            queryWrapper.eq("category_id", searchReq.getCategoryId());
        }
        if (searchReq.getTags() != null) {
            queryWrapper.eq("tags", searchReq.getTags());
        }
        List<Shop> shopList = shopMapper.selectList(queryWrapper);
        if (shopList == null) {
            throw new BussinessException(EmBusinessError.NO_OBJECT_FOUND, "搜索不到合适门店");
        }
        // 2、获取到合适的Do list后封装相关数据，转化为Bo list
        List<ShopBo> shopBoList = convertShopDoListToShopBoList(shopList);
        shopBoList.forEach(shopBo -> {
            double distance = CommonUtils.getDistance(shopBo.getLongitude().doubleValue(), shopBo.getLatitude().doubleValue(), searchReq.getLongitude().doubleValue(), searchReq.getLatitude().doubleValue());
            shopBo.setDistance((int) distance);
        });
        shopBoList.forEach(shopBo -> {
            System.out.println(shopBo.getDistance());
        });

        // 3、对Bo list进行排序
        // 使用默认排序或其他排序
        if (searchReq.getOrderBy() != null) {
            // oderby==1 低价排序 lowPriceOrder
            if (searchReq.getOrderBy() == 1) {
                Collections.sort(shopBoList, new Comparator<ShopBo>() {
                    @Override
                    public int compare(ShopBo o1, ShopBo o2) {
                        // 返回值为int类型，大于0表示正序，小于0表示逆序
                        int i = o1.getPricePerMan() - o2.getPricePerMan();
                        return i;
                    }
                });
            }
        } else {
            // order==null  默认排序 defaultOrder 推荐算法🐶
            Collections.sort(shopBoList, new Comparator<ShopBo>() {
                @Override
                public int compare(ShopBo o1, ShopBo o2) {
                    // (0.95*1/log10(distance)+ 0.05*remark_score/5)
                    int i = (int) (0.95 / Math.log10(o1.getDistance()) + 0.05 * o1.getRemarkScore().doubleValue() / 5
                            - 0.95 / Math.log10(o2.getDistance()) + 0.05 * o2.getRemarkScore().doubleValue() / 5);
                    return i;
                }
            });
        }

        // 实现2、直接在mapper中进行搜索
        // List<ShopBo> shopBoList = shopMapper.search(searchReq);

        return shopBoList;
    }

    @Override
    public List<Map<String, Object>> searchGroupByTags(SearchReq searchReq) throws BussinessException {

        // 实现2、直接在mapper中进行搜索
        List<Map<String, Object>> shopBoMapList = shopMapper.searchGroupByTags(searchReq);

        return shopBoMapList;
    }

    @Override
    public List<Map<String, Object>> searchGroupByTags(List<ShopBo> shopBoList) throws BussinessException {
        if (shopBoList == null) {
            return null;
        }
        List<Map<String, Object>> mapList = new CopyOnWriteArrayList<>();
        Map<String, Object> map = new ConcurrentHashMap<>();
        shopBoList.stream().map(ShopBo::getTags).forEach(shopBoTags -> {
            Integer count = (Integer) map.get(shopBoTags);
            if (count == null) {
                map.put(shopBoTags, 1);
            } else {
                map.put(shopBoTags, count+1);
            }
            mapList.add(map);
        });
        return mapList;
    }

        private ShopBo convertShopDoToShopBo (Shop shop){
            if (shop == null) {
                return null;
            }
            ShopBo shopBo = new ShopBo();
            BeanUtils.copyProperties(shop, shopBo);
            shopBo.setCategory(categoryService.getById(shopBo.getCategoryId()));
            shopBo.setSeller(sellerService.getById(shopBo.getSellerId()));
            return shopBo;
        }

        private List<ShopBo> convertShopDoListToShopBoList (List < Shop > shopList) {
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
