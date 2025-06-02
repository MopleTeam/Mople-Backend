package com.groupMeeting.core.aspect;

import com.groupMeeting.global.async.message.DiscordExceptionSender;
import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.data.exception.DiscordMessagePayload;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final DiscordExceptionSender exceptionSender;

    @Around("@annotation(com.groupMeeting.core.annotation.log.ApiLogging)")
    public Object processCustomAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Log request
        getInfo(request, joinPoint.getArgs());

        Object result = joinPoint.proceed();

        // Log response
        log.info(
                "Response - Method: {} URI: {} Payload: {} Duration: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                result,
                System.currentTimeMillis() - start
        );

        return result;
    }

    private static void getInfo(HttpServletRequest request, Object[] joinPoint) {
        log.info(
                "Request - Method: {} URI: {} Payload: {}",
                request.getMethod(),
                request.getRequestURI(),
                Arrays.toString(joinPoint)
        );
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