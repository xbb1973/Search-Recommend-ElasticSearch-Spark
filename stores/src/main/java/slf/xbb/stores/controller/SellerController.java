package slf.xbb.stores.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import slf.xbb.stores.entity.Seller;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.common.CommonReturnType;
import slf.xbb.stores.service.ISellerService;

import java.util.List;

/**
 * <p>
 * 商户 前端控制器
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@RestController
@RequestMapping("/stores/seller")
public class SellerController {

    @Autowired
    ISellerService sellerService;

    @RequestMapping("/get")
    @ResponseBody
    CommonReturnType getSeller(@RequestParam("id") Integer id) throws BussinessException {
        Seller seller = sellerService.getById(id);
        if (seller == null) {
            // return CommonReturnType.create(EmBusinessError.PARAMETER_VALIDATION_ERROR, "fail");
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "id不合法/ 数据库中找不到此id");
        }
        return CommonReturnType.create(seller);
    }

    @RequestMapping("/list")
    @ResponseBody
    CommonReturnType getSeller() throws BussinessException {
        List<Seller> sellerList = sellerService.list();
        if (sellerList == null) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        return CommonReturnType.create(sellerList);
    }

}
