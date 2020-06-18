package slf.xbb.stores.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Autowired
    private RestHighLevelClient restHighLevelClient;

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

    /**
     * 搜索算法实现：
     * 使用RestClient与ES进行交互
     *
     * @param searchReq
     * @return
     * @throws BussinessException
     */
    @Override
    public List<ShopBo> searchES(SearchReq searchReq) throws BussinessException, IOException {
        // 实现1、将排序逻辑放在service中，检索逻辑使用MBP QueryWrapper实现
        // 1、发送HTTP请求，与ES进行交互获取数据
        // 1.1、封装请求
        // SearchRequest searchRequest = new SearchRequest("shop");
        // SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // sourceBuilder.query(QueryBuilders.matchQuery("name", searchReq.getKeyword()));
        // sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // searchRequest.source(sourceBuilder);

        // 1.2、经由ES搜索排序后，获取到排序后的document id，根据id获得ShopBo
        // List<Integer> shopIdList = new ArrayList<>();
        // SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // SearchHit[] hitses = response.getHits().getHits();
        // for (SearchHit hit : hitses) {
        //     shopIdList.add(new Integer(hit.getSourceAsMap().get("id").toString()));
        // }
        //
        // List<ShopBo> shopBoList = shopIdList.stream().map(id -> {
        //     return get(id);
        // }).collect(Collectors.toList());

        // 1、发送HTTP请求，与ES进行交互获取数据
        // 1.1、封装请求
        Request request = new Request("GET", "/shop/_search");
        //构建请求
        JSONObject jsonRequestObj = new JSONObject();
        //构建source部分
        jsonRequestObj.put("_source", "*");


        @NotBlank(message = "关键字不许为空") String keyword = searchReq.getKeyword();
        @NotNull(message = "纬度不许为空") BigDecimal latitude = searchReq.getLatitude();
        @NotNull(message = "经度不许为空") BigDecimal longitude = searchReq.getLongitude();
        System.out.println(latitude);
        System.out.println(longitude);
        String tags = searchReq.getTags();
        Integer categoryId = searchReq.getCategoryId();
        Integer orderBy = searchReq.getOrderBy();

        // 对应的REST请求

        //构建自定义距离字段
        jsonRequestObj.put("script_fields", new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").put("distance", new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").put("script", new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("source", "haversin(lat, lon, doc['location'].lat, doc['location'].lon)");
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("lang", "expression");
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("params", new JSONObject());
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lat", latitude);
        jsonRequestObj.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lon", longitude);

        //构建query
        Map<String, Object> cixingMap = analyzeCategoryKeyword(keyword);
        boolean isAffectFilter = false;
        boolean isAffectOrder = true;
        jsonRequestObj.put("query", new JSONObject());


        //构建function score
        jsonRequestObj.getJSONObject("query").put("function_score", new JSONObject());

        //构建function score内的query
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("query", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").put("bool", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool").put("must", new JSONArray());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());

        //构建match query
        int queryIndex = 0;
        if (cixingMap.keySet().size() > 0 && isAffectFilter) {
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("bool", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").put("should", new JSONArray());
            int filterQueryIndex = 0;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .put("match", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").put("name", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("query", keyword);
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                    .getJSONObject("match").getJSONObject("name").put("boost", 0.1);

            for (String key : cixingMap.keySet()) {
                filterQueryIndex++;
                Integer cixingCategoryId = (Integer) cixingMap.get(key);
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").add(new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .put("term", new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").put("category_id", new JSONObject());
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("value", cixingCategoryId);
                jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                        .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("bool").getJSONArray("should").getJSONObject(filterQueryIndex)
                        .getJSONObject("term").getJSONObject("category_id").put("boost", 0);
            }
        } else {
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("match", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").put("name", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("query", keyword);
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("match").getJSONObject("name").put("boost", 0.1);
        }

        queryIndex++;
        //构建第二个query的条件
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").add(new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("seller_disabled_flag", 0);

        if (tags != null) {
            queryIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("tags", tags);
        }
        if (categoryId != null) {
            queryIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).put("term", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool")
                    .getJSONArray("must").getJSONObject(queryIndex).getJSONObject("term").put("category_id", categoryId);
        }


        //构建functions部分
        jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("functions", new JSONArray());

        int functionIndex = 0;
        if (orderBy == null) {
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("gauss", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss").put("location", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("origin", latitude.toString() + "," + longitude.toString());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("scale", "100km");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("offset", "0km");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("gauss")
                    .getJSONObject("location").put("decay", "0.5");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 9);

            functionIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "remark_score");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.2);

            functionIndex++;
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "seller_remark_score");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 0.1);


            if (cixingMap.keySet().size() > 0 && isAffectOrder) {
                for (String key : cixingMap.keySet()) {
                    functionIndex++;
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("filter", new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .put("term", new JSONObject());
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("filter")
                            .getJSONObject("term").put("category_id", cixingMap.get(key));
                    jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("weight", 3);

                }

            }
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "sum");
        } else {
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").add(new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).put("field_value_factor", new JSONObject());
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").getJSONArray("functions").getJSONObject(functionIndex).getJSONObject("field_value_factor")
                    .put("field", "price_per_man");

            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("score_mode", "sum");
            jsonRequestObj.getJSONObject("query").getJSONObject("function_score").put("boost_mode", "replace");
        }

        //排序字段
        jsonRequestObj.put("sort", new JSONArray());
        jsonRequestObj.getJSONArray("sort").add(new JSONObject());
        jsonRequestObj.getJSONArray("sort").getJSONObject(0).put("_score", new JSONObject());
        if (orderBy == null) {
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order", "desc");
        } else {
            jsonRequestObj.getJSONArray("sort").getJSONObject(0).getJSONObject("_score").put("order", "asc");
        }

        //聚合字段
        jsonRequestObj.put("aggs", new JSONObject());
        jsonRequestObj.getJSONObject("aggs").put("group_by_tags", new JSONObject());
        jsonRequestObj.getJSONObject("aggs").getJSONObject("group_by_tags").put("terms", new JSONObject());
        jsonRequestObj.getJSONObject("aggs").getJSONObject("group_by_tags").getJSONObject("terms").put("field", "tags");

        String reqJson = jsonRequestObj.toJSONString();
        System.out.println(reqJson);
        request.setJsonEntity(reqJson);
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        System.out.println(responseStr);
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArr = jsonObject.getJSONObject("hits").getJSONArray("hits");

        // 1.2、经由ES搜索排序后，获取到排序后的document id，根据id获得ShopBo
        List<Integer> shopIdList = new ArrayList<>();
        List<ShopBo> shopBoList = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            Integer id = new Integer(jsonObj.get("_id").toString());
            BigDecimal distance = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
            shopIdList.add(id);
            ShopBo shopBo = get(id);
            shopBo.setDistance(distance.intValue());
            shopBoList.add(shopBo);
        }
        // List<ShopBo> shopBoList = shopIdList.stream().map(this::get).collect(Collectors.toList());


        // List<ShopModel> shopModelList = new ArrayList<>();
        // for(int i = 0; i < jsonArr.size(); i++){
        //     JSONObject jsonObj = jsonArr.getJSONObject(i);
        //     Integer id = new Integer(jsonObj.get("_id").toString());
        //     BigDecimal distance = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
        //     ShopModel shopModel = get(id);
        //     shopModel.setDistance(distance.multiply(new BigDecimal(1000).setScale(0,BigDecimal.ROUND_CEILING)).intValue());
        //     shopModelList.add(shopModel);
        // }

        if (shopBoList == null) {
            throw new BussinessException(EmBusinessError.NO_OBJECT_FOUND, "搜索不到合适门店");
        }

        // 2、获取到合适的Do list后封装相关数据，转化为Bo list
        //      这里不需要了
        shopBoList.forEach(shopBo -> {
            System.out.println(shopBo.getDistance());
        });

        // 3、对Bo list进行排序
        // 使用默认排序或其他排序
        if (orderBy != null) {
            // oderby==1 低价排序 lowPriceOrder
            if (orderBy == 1) {
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
                    // int i = (int) (0.95 / Math.log10(o1.getDistance()) + 0.05 * o1.getRemarkScore().doubleValue() / 5
                    //         - 0.95 / Math.log10(o2.getDistance()) + 0.05 * o2.getRemarkScore().doubleValue() / 5);
                    int i = o1.getDistance() - o2.getDistance();
                    return i;
                }
            });
        }

        // 实现2、直接在mapper中进行搜索
        // List<ShopBo> shopBoList = shopMapper.search(searchReq);

        return shopBoList;
    }

    //构造分词函数识别器
    private Map<String,Object> analyzeCategoryKeyword(String keyword) throws IOException {
        Map<String,Object> res = new HashMap<>();

        Request request = new Request("GET","/shop/_analyze");
        request.setJsonEntity("{" + "  \"field\": \"name\"," + "  \"text\":\""+keyword+"\"\n" + "}");
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArray = jsonObject.getJSONArray("tokens");
        for(int i = 0; i < jsonArray.size(); i++){
            String token = jsonArray.getJSONObject(i).getString("token");
            Integer categoryId = getCategoryIdByToken(token);
            if(categoryId != null){
                res.put(token,categoryId);
            }
        }

        return res;
    }

    private Map<Integer,List<String>> categoryWorkMap = new HashMap<>();
    private Integer getCategoryIdByToken(String token){
        for(Integer key : categoryWorkMap.keySet()){
            List<String> tokenList = categoryWorkMap.get(key);
            if(tokenList.contains(token)){
                return key;
            }
        }
        return null;
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
        List<Map<String, Object>> mapList = new ArrayList<>()<>();
        Map<String, Object> map = new HashMap<>();
        shopBoList.stream().map(ShopBo::getTags).forEach(shopBoTags -> {
            Integer count = (Integer) map.get(shopBoTags);
            if (count == null) {
                map.put(shopBoTags, 1);
            } else {
                map.put(shopBoTags, count + 1);
            }
            mapList.add(map);
        });
        return mapList;
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
