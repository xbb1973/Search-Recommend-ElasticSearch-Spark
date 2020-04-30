package slf.xbb.stores.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.common.EmBusinessError;
import slf.xbb.stores.entity.User;
import slf.xbb.stores.mapper.UserMapper;
import slf.xbb.stores.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Override
    @Transactional
    public User register(User user) throws BussinessException, NoSuchAlgorithmException {
        LocalDateTime date = LocalDateTime.now();
        user.setCreatedAt(date);
        user.setUpdatedAt(date);
        user.setPassword(encodeByMd5(user.getPassword()));
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException de) {
            throw new BussinessException(EmBusinessError.REGISTER_DUP_FAIL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return getById(user.getId());
    }

    /*
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        // 2、名字中包含雨年并且龄大于等于20且小于等于40并且email不为空
        //    name like '%雨%' and age between 20 and 40 and email is not null
        // .select查询显示指定列
        queryWrapper.select("name", "age").like("name", "雨").between("age", 20, 40).isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::print);

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(User::getName, User::getAge).like(User::getName, "斑斑");
        List<User> userss = userMapper.selectList(lambdaQueryWrapper);
        userss.forEach(System.out::print);
     */

    @Override
    @Transactional
    public User login(String telphone, String unEncryptPassword) throws BussinessException, NoSuchAlgorithmException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select().eq("telphone", telphone).eq("password", encodeByMd5(unEncryptPassword));
        User user = userMapper.selectOne(queryWrapper);
        if (user == null){
            throw new BussinessException(EmBusinessError.USER_NOT_EXIST);
        }
        return user;
    }

    private String encodeByMd5(String password) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(messageDigest.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

}
