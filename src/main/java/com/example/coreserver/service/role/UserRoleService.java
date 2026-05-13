package com.example.coreserver.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.UserRole;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
public interface UserRoleService extends IService<UserRole> {

    UserRole getUserRoleById(int userId, int roleId);

    List<UserRole> getUserRolesByUserId(int userId);

    List<UserRole> getUserRolesByRoleId(int roleId);
}
