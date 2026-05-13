package com.example.coreserver.aspect;

import com.alibaba.fastjson2.JSON;
import com.example.coreserver.entity.log.ExceptionLog;
import com.example.coreserver.event.ExceptionLogEvent;
import com.example.coreserver.utils.ExceptionUtil;
import com.example.coreserver.utils.IpUtil;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author lord
 * @date 2025/4/4
 * @description 错误日志
 */
@Aspect
@Component
public class ExceptionLogAspect {

    @Autowired
    private ApplicationContext applicationContext;

    @Pointcut("execution(* com.example.coreserver.controller..*.*(..))")
    public void exceptionLogPointcut(){}

    @AfterThrowing(value = "exceptionLogPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (HttpServletRequest) Objects.requireNonNull(requestAttributes).resolveReference(RequestAttributes.REFERENCE_REQUEST);
        ExceptionLog exceptionLog = new ExceptionLog();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
        exceptionLog.setOptUri(Objects.requireNonNull(request).getRequestURI());
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        methodName = className + "." + methodName;
        exceptionLog.setOptMethod(methodName);
        exceptionLog.setRequestMethod(Objects.requireNonNull(request).getMethod());
        if (joinPoint.getArgs().length > 0){
            if (joinPoint.getArgs()[0] instanceof MultipartFile){
                exceptionLog.setRequestParam("file");
            } else {
                exceptionLog.setRequestParam(JSON.toJSONString(joinPoint.getArgs()));
            }
        }
        if (Objects.nonNull(apiOperation)){
            exceptionLog.setOptDesc(apiOperation.value());
        } else {
            exceptionLog.setOptDesc("");
        }
        exceptionLog.setExceptionInfo(ExceptionUtil.getTrace(e));
        String ipAddress = IpUtil.getIpAddress(request);
        exceptionLog.setIpAddress(ipAddress);
        exceptionLog.setIpSource(IpUtil.getIpSource(ipAddress));
        applicationContext.publishEvent(new ExceptionLogEvent(exceptionLog));
    }
}
