package com.skripsi.siap_sewa.logging;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.skripsi.siap_sewa..controller..*(..)) ||" +
            " execution(* com.skripsi.siap_sewa..service..*(..)) || " +
            "execution(* com.skripsi.siap_sewa..repository..*(..))")
    public void applicationMethods() {

    }

    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        String requestHeaders = (requestAttributes != null) ? requestAttributes.toString() : "No Headers";
        String requestBody = Arrays.toString(joinPoint.getArgs());

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Trace ID: " + traceId);
        System.out.println("Endpoint yang terhit: " + getRequestUri());
        System.out.println("Header : " + requestHeaders);
        System.out.println("Request Body : " + requestBody);
        System.out.println("--------------------------------------------------------------------------------");

        long startTime = System.currentTimeMillis();
        System.out.println("Starting method " + joinPoint.getSignature().getName() + " from " + joinPoint.getTarget().getClass().getSimpleName());

        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Finishing method " + joinPoint.getSignature().getName() + " from " + joinPoint.getTarget().getClass().getSimpleName() + " in " + executionTime + " ms");
        System.out.println("Final Response: " + result);
        System.out.println("--------------------------------------------------------------------------------");

        return result;
    }

    private String getRequestUri() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            Object contextPath = requestAttributes.getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT_PATH", RequestAttributes.SCOPE_REQUEST);
            if (contextPath != null) {
                return contextPath.toString();
            }
        }
        return "Unknown URI";
    }

}
