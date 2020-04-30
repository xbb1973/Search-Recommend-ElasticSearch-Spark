package slf.xbb.stores.controller;


import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.CommonReturnType;
import slf.xbb.stores.common.CommonUtils;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.User;
import slf.xbb.stores.service.IUserService;
import slf.xbb.stores.vo.LoginReq;
import slf.xbb.stores.vo.RegistryReq;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author xbb
 * @since 2020-04-13
 */
@Controller("/user")
@RequestMapping("/stores/user")
// @RequestMapping("/user")
public class UserController {

    public static String CURRENT_USER_SESSION = "currentUserSession";

    @Autowired
    IUserService userService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @RequestMapping("/index")
    public ModelAndView index() {
        String userName = "wtt";
        // 根文件路径为thymeleaf的templates文件夹
        ModelAndView modelAndView = new ModelAndView("/index.html");
        modelAndView.addObject("name", userName);
        return modelAndView;
    }

    @RequestMapping("/regist")
    @ResponseBody
    public CommonReturnType regist(@Param("telphone") String telphone,
                                   @Param("password") String password,
                                   @Param("nickName")String nickName,
                                   @Param("gender")Integer gender) throws NoSuchAlgorithmException, BussinessException {
        // localhost:8080/stores/user/regist?telphone=13225000602&password=123&nickName=wtt&gender=1
        // if (bindingResult.hasErrors()) {
        //     throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        // }
        User user = new User();
        user.setTelphone(telphone);
        user.setPassword(password);
        user.setNickName(nickName);
        user.setGender(gender);
        User registerUser = null;
        registerUser = userService.register(user);
        return CommonReturnType.create(registerUser);
    }

    @RequestMapping("/register")
    @ResponseBody
    public CommonReturnType register(@Valid @RequestBody RegistryReq registryReq, BindingResult bindingResult) throws BussinessException, NoSuchAlgorithmException {
        if (bindingResult.hasErrors()) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        User user = new User();
        user.setTelphone(registryReq.getTelphone());
        user.setPassword(registryReq.getPassword());
        user.setNickName(registryReq.getNickName());
        user.setGender(registryReq.getGender());
        User registerUser = userService.register(user);
        return CommonReturnType.create(registerUser);
    }

    @RequestMapping("/login")
    @ResponseBody
    public CommonReturnType login(@Valid @RequestBody LoginReq loginReq, BindingResult bindingResult) throws BussinessException, NoSuchAlgorithmException {
        if (bindingResult.hasErrors()) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, CommonUtils.processErrorString(bindingResult));
        }
        User user = userService.login(loginReq.getTelphone(), loginReq.getPassword());
        httpServletRequest.getSession().setAttribute(CURRENT_USER_SESSION, user);
        return CommonReturnType.create(user);
    }
    @RequestMapping("/logout")
    @ResponseBody
    public CommonReturnType logout() {
        httpServletRequest.getSession().invalidate();
        return CommonReturnType.create(null);
    }

    @RequestMapping("/getCurrentUser")
    @ResponseBody
    public CommonReturnType getCurrentUser(){
        User user = (User) httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION);
        return CommonReturnType.create(user);
    }
}
