package com.skripsi.siap_sewa.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.skripsi.siap_sewa.controller..*(..))")
    public void logControllerMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.info("Method {} in {} called with arguments: {}", joinPoint.getSignature().getName(), className, Arrays.toString(joinPoint.getArgs()));
    }

    @Before("execution(* com.skripsi.siap_sewa.service..*(..))")
    public void logServiceMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.info("Method {} in {} called with arguments: {}", joinPoint.getSignature().getName(), className, Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(value = "execution(* com.skripsi.siap_sewa.controller..*(..))", returning = "result")
    public void logControllerMethodExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.info("Method {} in {} returned with: {}", joinPoint.getSignature().getName(), className, result);
    }

    @AfterReturning(value = "execution(* com.skripsi.siap_sewa.service..*(..))", returning = "result")
    public void logServiceMethodExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.info("Method {} in {} returned with: {}", joinPoint.getSignature().getName(), className, result);
    }

    @AfterThrowing(value = "execution(* com.skripsi.siap_sewa.controller..*(..))", throwing = "exception")
    public void logControllerMethodError(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("Method {} in {} threw exception: {}", joinPoint.getSignature().getName(), className, exception.getMessage());
    }

    @AfterThrowing(value = "execution(* com.skripsi.siap_sewa.service..*(..))", throwing = "exception")
    public void logServiceMethodError(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("Method {} in {} threw exception: {}", joinPoint.getSignature().getName(), className, exception.getMessage());
    }
}

