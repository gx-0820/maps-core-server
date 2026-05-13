package com.example.coreserver.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 生成jwt令牌相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {


    private String secretKey; // jwt签名加密秘钥
    private long ttl; // jwt过期时间
    private String tokenName; // jwt签名加密秘钥

}


