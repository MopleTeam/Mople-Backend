package com.mople.core.aspect;

import com.mople.global.async.message.DiscordExceptionSender;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.DiscordMessagePayload;
import com.mople.global.event.data.exception.DiscordField;
import com.mople.global.event.filter.DiscordAlertFilter;

import com.mople.global.logging.LoggingContextManager;
import com.mople.global.logging.SensitiveLogger;
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

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class BusinessLogicLoggingAspect {
    private final BusinessLogicLogger businessLogicLogger;
    private final LoggingContextManager loggingContextManager;
    private final DiscordExceptionSender exceptionSender;
    private final SensitiveLogger sensitiveLogger;
    private final DiscordAlertFilter discordAlertFilter;

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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Discord 알림이 필요한 경우만 전송
        if (discordAlertFilter.shouldAlert(ex, request)) {
            var discordMessage = DiscordMessage
                    .builder()
                    .content("🚨 **Server Exception Detected**")
                    .embeds(
                            List.of(
                                    DiscordMessagePayload.builder()
                                            .title("🎯 " + ex.getClass().getSimpleName())
                                            .color(15158332) // 빨간색
                                            .fields(List.of(
                                                    DiscordField.createField("Method", point.getSignature().toShortString(), true),
                                                    DiscordField.createField("Request", request.getMethod() + " " + request.getRequestURI(), true),
                                                    DiscordField.createField("Request ID", loggingContextManager.getRequestId(), true),
                                                    DiscordField.createField("Error Message", ex.getMessage(), false),
                                                    DiscordField.createField("Stack Trace", "```\n" + getStackTracePreview(ex) + "```", false)
                                            ))
                                            .build()
                            )
                    )
                    .build();

            exceptionSender.exceptionSend(discordMessage);
        }
    }

    private String getStackTracePreview(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();

        if (stackTrace.length == 0) {
            return "stack trace empty";
        }

        StringBuilder preview = new StringBuilder();
        int limit = Math.min(5, stackTrace.length);

        for (int i = 0; i < limit; i++) {
            preview.append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > 5) {
            preview.append("... and ").append(stackTrace.length - 5).append(" more");
        }

        return preview.toString();
    }
}