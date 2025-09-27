package com.ads.mygateway.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogingAspect {
    Logger logger = LogManager.getLogger(LogingAspect.class);
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void pointCutMethod(){
    }

    @Around("pointCutMethod()")
    public Object logingmethod(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("CustomerService:saveCutomer execution started..");
        Object result = joinPoint.proceed();
        logger.info("CustomerService:saveCutomer execution ended..");
        return result;
    }
}
