package com.mople.core.aspect;

import com.mople.global.event.notifier.HttpDiscordAlertNotifier;
import com.mople.global.logging.LoggingContextManager;
import com.mople.global.logging.logger.BusinessLogicLogger;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class BusinessLogicLoggingAspect {
    private final BusinessLogicLogger businessLogicLogger;
    private final LoggingContextManager loggingContextManager;
    private final HttpDiscordAlertNotifier httpDiscordAlertNotifier;

    @Around("@annotation(com.mople.core.annotation.log.BusinessLogicLogging)")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        loggingContextManager.setRequestId();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            businessLogicLogger.logComplete(executionTime);

            return result;
        } catch (Exception ex) {
            businessLogicLogger.logError(ex.getMessage());
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "execution(* com.mople.*.controller.*.*(..))", throwing = "ex")
    public void processControllerExceptionThrown(JoinPoint point, Throwable ex) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        httpDiscordAlertNotifier.notify(point, request, ex);
    }
}