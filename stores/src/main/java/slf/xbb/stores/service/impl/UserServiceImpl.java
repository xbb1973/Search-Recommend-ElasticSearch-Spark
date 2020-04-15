package slf.xbb.stores.service.impl;

import slf.xbb.stores.entity.User;
import slf.xbb.stores.mapper.UserMapper;
import slf.xbb.stores.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
