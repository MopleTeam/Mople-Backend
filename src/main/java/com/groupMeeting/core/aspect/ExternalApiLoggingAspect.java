package com.groupMeeting.core.aspect;

import com.groupMeeting.global.logging.logger.ExternalApiLogger;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiLoggingAspect {
    private final ExternalApiLogger externalApiLogger;

    @Around("@annotation(com.groupMeeting.core.annotation.log.ExternalApiLogging)")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            externalApiLogger.logComplete(executionTime);

            return result;
        } catch (Exception ex) {
            externalApiLogger.logError(ex.getMessage());
            throw ex;
        }
    }
}
