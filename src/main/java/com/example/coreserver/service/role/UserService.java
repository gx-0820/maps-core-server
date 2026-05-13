package com.example.coreserver.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.dto.UserDTO;
import com.example.coreserver.entity.User;
import com.example.coreserver.vo.UserLoginVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
public interface UserService extends IService<User> {

    UserLoginVo Login(UserDTO userDTO);

    User getUserByUsername(String username);

}
