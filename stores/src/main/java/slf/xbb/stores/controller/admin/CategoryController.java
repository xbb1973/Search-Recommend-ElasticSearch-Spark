package slf.xbb.stores.controller.admin;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;
import slf.xbb.stores.aspect.AdminPermission;
import slf.xbb.stores.common.*;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.service.ICategoryService;
import slf.xbb.stores.vo.PageQuery;
import slf.xbb.stores.vo.CategoryCreateReq;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 商家 前端控制器
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Controller("/admin/category")
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    public static String controllerAttributeName = "CONTROLLER_NAME";
    public static String actionAttributeName = "ACTION_NAME";
    public static String controllerAttributeValue = "category";

    @RequestMapping("/index")
    @AdminPermission
    public ModelAndView index(PageQuery pageQuery)  {
        // 分页
        PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize());
        //categoryService.list
        List<Category> categoryList = categoryService.list();
        PageInfo<Category> categoryPageInfo = new PageInfo<>(categoryList);

        ModelAndView modelAndView = new ModelAndView("/category/index.html");
        modelAndView.addObject("data", categoryPageInfo);
        modelAndView.addObject(controllerAttributeName, controllerAttributeValue);
        modelAndView.addObject(controllerAttributeValue, "index");
        return modelAndView;
    }

    @RequestMapping("/createPage")
    @AdminPermission
    public ModelAndView createPage() {
        ModelAndView modelAndView = new ModelAndView("/category/create.html");
        modelAndView.addObject(controllerAttributeName, controllerAttributeValue);
        modelAndView.addObject(controllerAttributeValue, "create");
        return modelAndView;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @AdminPermission
    public String create(@Valid CategoryCreateReq categoryCreateReq, BindingResult bindingResult) throws BussinessException {
        if (bindingResult.hasErrors()){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        Category category = new Category();
        category.setName(categoryCreateReq.getName());
        category.setIconUrl(categoryCreateReq.getIconUrl());
        category.setSort(categoryCreateReq.getSort());
        categoryService.create(category);
        return "redirect:/admin/category/index";
    }

    @RequestMapping("/get")
    @ResponseBody
    @AdminPermission(produceType = "application/json")
    public CommonReturnType getCategory(@RequestParam("id") Integer id) throws BussinessException {
        Category category = categoryService.getById(id);
        if (category == null) {
            // return CommonReturnType.create(EmBusinessError.PARAMETER_VALIDATION_ERROR, "fail");
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "id不合法/ 数据库中找不到此id");
        }
        return CommonReturnType.create(category);
    }
}
