package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.Role;
import com.example.coreserver.mapper.RoleMapper;
import com.example.coreserver.service.role.RoleService;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Role getRoleByName(String roleName) {
        return roleMapper.selectOne(new QueryWrapper<Role>().eq("role_name", roleName));
    }

    @Override
    public Role getRoleByKey(String key) {
        return roleMapper.selectOne(new QueryWrapper<Role>().eq("role_key", key));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }
}
