package com.example.coreserver.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.RolePermission;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
public interface RolePermissionService extends IService<RolePermission> {

    /**
     * 根据角色ID和权限ID查询角色权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 角色权限对象
     */
    RolePermission getRolePermissionById(int roleId, int permissionId);


    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<RolePermission> getPermissionsByRoleId(int roleId);

    /**
     * 根据权限ID获取角色列表
     *
     * @param permissionId 权限ID
     * @return 角色列表
     */
    List<RolePermission> getRolesByPermissionId(int permissionId);
}
