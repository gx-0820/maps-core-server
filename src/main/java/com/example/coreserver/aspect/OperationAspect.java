package com.example.coreserver.aspect;

import com.example.coreserver.controller.OperationSseController;
import io.swagger.v3.oas.annotations.Operation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationAspect {

    private final OperationSseController operationSseController;

    public OperationAspect(OperationSseController operationSseController) {
        this.operationSseController = operationSseController;
    }

    @Around("@annotation(io.swagger.v3.oas.annotations.Operation)")
    public Object aroundOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Operation operation = signature.getMethod().getAnnotation(Operation.class);
        
        if (operation != null) {
            // 发送操作信息
            operationSseController.sendOperation(operation.summary());
        }
        
        // 执行原方法
        return joinPoint.proceed();
    }
} 