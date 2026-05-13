package com.example.coreserver.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.Role;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据角色名称查询角色
     *
     * @param roleName 角色名称
     * @return 角色对象
     */
    Role getRoleByName(String roleName);

    /**
     * 根据角色key查询角色
     *
     * @param key 角色key
     * @return 角色对象
     */
    Role getRoleByKey(String key);


    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> getAllRoles();
}
