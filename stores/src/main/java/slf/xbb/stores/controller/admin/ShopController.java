package slf.xbb.stores.controller.admin;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import slf.xbb.stores.aspect.AdminPermission;
import slf.xbb.stores.bo.ShopBo;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.CommonReturnType;
import slf.xbb.stores.common.CommonUtils;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.Shop;
import slf.xbb.stores.service.IShopService;
import slf.xbb.stores.vo.ShopCreateReq;
import slf.xbb.stores.vo.PageQuery;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 门店 前端控制器
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Controller("/admin/shop")
@RequestMapping("/admin/shop")
public class ShopController {


    @Autowired
    private IShopService shopService;

    public static String controllerAttributeName = "CONTROLLER_NAME";
    public static String actionAttributeName = "ACTION_NAME";
    public static String controllerAttributeValue = "shop";

    @RequestMapping("/list")
    @ResponseBody
    @AdminPermission(produceType = "application/json")
    public CommonReturnType list()  {
        List<ShopBo> shopBoList = shopService.getShopList();
        return CommonReturnType.create(shopBoList);
    }

    // @RequestMapping("/index")
    // @AdminPermission
    // public ModelAndView index(PageQuery pageQuery)  {
    //     // 分页
    //     PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize());
    //     List<ShopBo> shopBoList = shopService.getShopList();
    //     PageInfo<ShopBo> shopPageInfo = new PageInfo<>(shopBoList);
    //
    //     ModelAndView modelAndView = new ModelAndView("/shop/index.html");
    //     modelAndView.addObject("data", shopPageInfo);
    //     modelAndView.addObject(controllerAttributeName, controllerAttributeValue);
    //     modelAndView.addObject(controllerAttributeValue, "index");
    //     return modelAndView;
    // }

    @RequestMapping("/index")
    @AdminPermission
    public ModelAndView index(PageQuery pageQuery)  {
        PageInfo<ShopBo> shopBoPageInfo = shopService.getPage(pageQuery);

        ModelAndView modelAndView = new ModelAndView("/shop/index.html");
        modelAndView.addObject("data", shopBoPageInfo);
        modelAndView.addObject(controllerAttributeName, controllerAttributeValue);
        modelAndView.addObject(controllerAttributeValue, "index");
        return modelAndView;
    }

    @RequestMapping("/createPage")
    @AdminPermission
    public ModelAndView createPage() {
        ModelAndView modelAndView = new ModelAndView("/shop/create.html");
        modelAndView.addObject(controllerAttributeName, controllerAttributeValue);
        modelAndView.addObject(controllerAttributeValue, "create");
        return modelAndView;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @AdminPermission
    public String create(@Valid ShopCreateReq shopCreateReq, BindingResult bindingResult) throws BussinessException {
        if (bindingResult.hasErrors()){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopCreateReq, shop);
        shopService.create(shop);
        return "redirect:/admin/shop/index";
    }

    @RequestMapping("/get")
    @ResponseBody
    @AdminPermission(produceType = "application/json")
    public CommonReturnType getShop(@RequestParam("id") Integer id) throws BussinessException {
        ShopBo shopBo = shopService.get(id);
        if (shopBo == null) {
            // return CommonReturnType.create(EmBusinessError.PARAMETER_VALIDATION_ERROR, "fail");
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "id不合法/ 数据库中找不到此id");
        }
        return CommonReturnType.create(shopBo);
    }
}
