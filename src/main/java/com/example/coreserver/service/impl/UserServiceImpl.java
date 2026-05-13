package com.example.coreserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.dto.UserDTO;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.User;
import com.example.coreserver.entity.UserLogin;
import com.example.coreserver.jwt.JwtClaimsConstant;
import com.example.coreserver.jwt.JwtProperties;
import com.example.coreserver.mapper.UserMapper;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.role.UserService;
import com.example.coreserver.utils.BaseContext;
import com.example.coreserver.utils.ConfigUtils;
import com.example.coreserver.utils.JwtUtil;
import com.example.coreserver.utils.RedisUtils;
import com.example.coreserver.vo.UserLoginVo;
import com.example.coreserver.wsserver.services.CollectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhn
 * @since 2025-03-10
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private ConfigService configService;

    public UserLoginVo Login(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();

        // 1. 封装用户登录表单，创建未认证Authentication对象
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        // 2. 进行校验
        Authentication authenticate = authenticationManager.authenticate(authentication);
        // 3. 获取用户信息
        if (Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或密码错误");
        }
        UserLogin userLogin = (UserLogin) authenticate.getPrincipal();
        User user = userLogin.getUser();
        if (user.getStatus().equals(1)){
            throw new RuntimeException("账号被禁用");
        }
        log.info("用户 {} 登录成功", userLogin.getUser().getUsername());

        // 登录成功，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        // 使用fastjson的方法，把对象转换成json字符串
        String loginEmpString = JSON.toJSONString(userLogin);
        claims.put(JwtClaimsConstant.USER_LOGIN, loginEmpString);
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);

        // 存储redis白名单
        String tokenKey = "token:" + token;
        redisUtil.setEx(tokenKey, token, jwtProperties.getTtl(), TimeUnit.MILLISECONDS);

        BaseContext.setCurrentId(user);

        List<Config> configKeys = configService.getConfigKeys(Arrays.asList("sys.location", "sys.version"));

        Config location = ConfigUtils.getConfig.apply("sys.location", configKeys);
        Config version = ConfigUtils.getConfig.apply("sys.version", configKeys);

        if(location == null || version == null){
            throw new RuntimeException("参数未设置【系统部署所在地（sys.location），系统部署版本（sys.version）】");
        }

        //3、返回实体对象
        return UserLoginVo.builder()
                .id(user.getId())
                .token(token)
                .username(user.getUsername())
                .version(version.getConfigValue())
                .location(location.getConfigValue())
                .build();

    }


    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

}
