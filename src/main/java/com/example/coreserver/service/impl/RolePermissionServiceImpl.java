package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.RolePermission;
import com.example.coreserver.mapper.RolePermissionMapper;
import com.example.coreserver.service.role.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public RolePermission getRolePermissionById(int roleId, int permissionId) {
        return rolePermissionMapper.selectOne(new QueryWrapper<RolePermission>().eq("role_id", roleId).eq("permission_id", permissionId));
    }


    @Override
    public List<RolePermission> getPermissionsByRoleId(int roleId) {
        return rolePermissionMapper.selectList(new QueryWrapper<RolePermission>().eq("role_id", roleId));
    }

    @Override
    public List<RolePermission> getRolesByPermissionId(int permissionId) {
        return rolePermissionMapper.selectList(new QueryWrapper<RolePermission>().eq("permission_id", permissionId));
    }
}
