package slf.xbb.stores.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.CommonReturnType;
import slf.xbb.stores.common.CommonUtils;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.service.ICategoryService;
import slf.xbb.stores.service.IShopService;
import slf.xbb.stores.vo.RecommendReq;
import slf.xbb.stores.vo.SearchReq;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * 门店 前端控制器
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@RestController("/stores/shop")
@RequestMapping("/stores/shop")
public class ShopController {

    // 门店服务
    @Autowired
    IShopService shopService;

    // 内容相关过滤
    @Autowired
    ICategoryService categoryService;

    /**
     * 推荐服务v1.0
     * @param recommendReq
     * @param bindingResult
     * @return
     */
    @RequestMapping("/recommend")
    @ResponseBody
    public CommonReturnType recommend(@Valid RecommendReq recommendReq, BindingResult bindingResult) throws BussinessException {
        if (bindingResult.hasErrors()){
            return CommonReturnType.create(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        List<ShopBo> shopBoList = shopService.recommend(recommendReq);
        return CommonReturnType.create(shopBoList);
    }

    @RequestMapping("/search")
    @ResponseBody
    public CommonReturnType search(@Valid SearchReq searchReq, BindingResult bindingResult) throws BussinessException {
        if (bindingResult.hasErrors()){
            return CommonReturnType.create(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        List<ShopBo> shopBoList = shopService.search(searchReq);
        List<Category> categoryList = categoryService.list();
        List<Map<String, Object>> tagsAggregation = shopService.searchGroupByTags(searchReq);
        // 后续仍需扩展筛选，所以需要保存在map中
        Map<String, Object> resultMap = new ConcurrentHashMap<>();
        resultMap.put("shop", shopBoList);
        resultMap.put("category", categoryList);
        resultMap.put("tags", tagsAggregation);
        return CommonReturnType.create(resultMap);
    }
}