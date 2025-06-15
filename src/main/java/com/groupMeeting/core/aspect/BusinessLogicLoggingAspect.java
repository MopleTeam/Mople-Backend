package com.groupMeeting.core.aspect;

import com.groupMeeting.global.async.message.DiscordExceptionSender;
import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.data.exception.DiscordMessagePayload;

import com.groupMeeting.global.logging.LoggingContextManager;
import com.groupMeeting.global.logging.logger.BusinessLogicLogger;
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

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class BusinessLogicLoggingAspect {
    private final BusinessLogicLogger businessLogicLogger;
    private final LoggingContextManager loggingContextManager;
    private final DiscordExceptionSender exceptionSender;

    @Around("@annotation(com.groupMeeting.core.annotation.log.BusinessLogicLogging)")
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

    @AfterThrowing(pointcut = "execution(* com.groupMeeting.*.controller.*.*(..))", throwing = "ex")
    public void processControllerExceptionThrown(JoinPoint point, Throwable ex) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String message = ("""
                Request - Method: %s\s
                URI: %s
                Arguments: %s
                exception: %s(message=%s)
                Stacktrace: %s
                """)
                .formatted(
                        request.getMethod(),
                        request.getRequestURI(),
                        Arrays.toString(point.getArgs()),
                        ex.getClass(),
                        ex.getMessage(),
                        Arrays.toString(new String[]{Arrays.toString(ex.getStackTrace()).substring(0, 1000)})
                );

        var discordMessage = DiscordMessage
                .builder()
                .content("# Server Exception")
                .embeds(
                        List.of(
                                DiscordMessagePayload.builder()
                                        .title("ℹ️Info")
                                        .description(message)
                                        .build()
                        )
                )
                .build();

        exceptionSender.exceptionSend(discordMessage);
    }
}