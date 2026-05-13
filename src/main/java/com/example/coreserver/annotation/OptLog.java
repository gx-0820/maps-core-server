package com.example.coreserver.annotation;

import java.lang.annotation.*;

/**
 * @author lord
 * @date 2025/4/4
 * @description 自定义操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptLog {
    String optType() default "";
}
