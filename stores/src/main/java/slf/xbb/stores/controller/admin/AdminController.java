package slf.xbb.stores.controller.admin;

import com.alibaba.druid.util.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import slf.xbb.stores.aspect.AdminPermission;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/30 1:21 下午
 * @description：运营后台管理员
 * @modifiedBy：
 * @version:
 */
@Controller("/admin/admin")
@RequestMapping("/admin/admin")
public class AdminController {

    @Value("${admin.email}")
    private String email;

    @Value("${admin.encryptPassword}")
    private String encryptPassword;

    @Autowired
    HttpServletRequest httpServletRequest;

    public static String CURRENT_ADMIN_SESSION = "currentAdminSession";

    // @AdminPermission(produceType = "application/json")
    // @RequestMapping("/index")
    // @ResponseBody
    // public CommonReturnType index() {
    //     return CommonReturnType.create(null);
    // }

    @AdminPermission(produceType = "text/html")
    @RequestMapping("/index")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("/admin/index");
        return modelAndView;
    }

    @RequestMapping("/loginPage")
    public ModelAndView loginPage() {
        ModelAndView modelAndView = new ModelAndView("/admin/login");
        return modelAndView;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@Param("email") String email,
                              @Param("password") String password) throws BussinessException, NoSuchAlgorithmException {
        if (StringUtils.isEmpty(email)||StringUtils.isEmpty(password)){
            throw new BussinessException(EmBusinessError.LOGIN_FAIL, "email or password不许为空");
        }

        if (email.equals(this.email) && encodeByMd5(password).equals(encryptPassword)) {
            httpServletRequest.getSession().setAttribute(CURRENT_ADMIN_SESSION, email);
            return "redirect:/admin/admin/index";
        } else {
            throw new BussinessException(EmBusinessError.LOGIN_FAIL, "email or password密码错误");
        }
    }

    private String encodeByMd5(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(messageDigest.digest(password.getBytes(StandardCharsets.UTF_8)));
    }
}
