package slf.xbb.stores.service;

import slf.xbb.stores.common.BussinessException;
import slf.xbb.stores.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author xbb
 * @since 2020-04-15
 */
public interface IUserService extends IService<User> {
    public User register(User user) throws BussinessException, NoSuchAlgorithmException;

    public User login(String telphone, String unEncryptPassword) throws BussinessException, NoSuchAlgorithmException;
}
