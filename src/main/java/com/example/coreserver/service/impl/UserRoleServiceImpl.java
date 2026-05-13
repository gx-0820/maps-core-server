package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.UserRole;
import com.example.coreserver.mapper.UserRoleMapper;
import com.example.coreserver.service.role.UserRoleService;
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
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserRole getUserRoleById(int userId, int roleId) {
        return userRoleMapper.selectOne(new QueryWrapper<UserRole>().eq("user_id", userId).eq("role_id", roleId));
    }

    @Override
    public List<UserRole> getUserRolesByUserId(int userId) {
        return userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", userId));
    }

    @Override
    public List<UserRole> getUserRolesByRoleId(int roleId) {
        return userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("role_id", roleId));
    }
}
