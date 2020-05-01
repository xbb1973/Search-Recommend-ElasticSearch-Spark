package slf.xbb.stores.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import slf.xbb.stores.aspect.AdminPermission;
import slf.xbb.stores.common.*;
import slf.xbb.stores.entity.Category;
import slf.xbb.stores.service.ICategoryService;
import slf.xbb.stores.vo.CategoryCreateReq;
import slf.xbb.stores.vo.PageQuery;

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
@Controller("/stores/category")
@RequestMapping("/stores/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    public static String controllerAttributeName = "CONTROLLER_NAME";
    public static String actionAttributeName = "ACTION_NAME";
    public static String controllerAttributeValue = "category";

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType list(/*PageQuery pageQuery*/) {
        // 分页
        // PageHelper.startPage(pageQuery.getPage(), pageQuery.getSize());
        //categoryService.list
        List<Category> categoryList = categoryService.list();
        // PageInfo<Category> categoryPageInfo = new PageInfo<>(categoryList);

        return CommonReturnType.create(categoryList);
    }
}
