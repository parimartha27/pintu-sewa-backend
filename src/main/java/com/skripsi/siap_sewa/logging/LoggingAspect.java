package com.skripsi.siap_sewa.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.skripsi.siap_sewa.controller..*(..)) || execution(* com.skripsi.siap_sewa.service..*(..))")
    public void appMethods() {}

    @Before("appMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Entering {}.{}() with arguments: {}", traceId, className, methodName, Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "appMethods()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("{} Exiting {}.{}() returned: {}", traceId, className, methodName, result);
    }

    @AfterThrowing(pointcut = "appMethods()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Exception ex) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.error("{} Exception in {}.{}() with message: {}", traceId, className, methodName, ex.getMessage(), ex);
    }
}
