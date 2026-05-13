package com.example.coreserver.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.coreserver.entity.User;
import com.example.coreserver.entity.UserLogin;
import com.example.coreserver.mapper.PermissionMapper;
import com.example.coreserver.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 根据用户名查询用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (username.isEmpty()){
            throw new InternalAuthenticationServiceException("");
        }
        //  根据用户名查询用户信息
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        // 判断是否查到用户 如果没查到抛出异常
        if (ObjectUtil.isNull(user)){
            throw new UsernameNotFoundException("");
        }
        // 2.赋权操作 查询数据库
        List<String> list = permissionMapper.getPermissionByUserId(user.getId());

        log.info(list.toString());

        return new UserLogin(user, list);
    }
}

