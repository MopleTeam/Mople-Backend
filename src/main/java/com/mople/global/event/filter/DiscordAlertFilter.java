package com.mople.global.event.filter;

import com.mople.core.exception.custom.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class DiscordAlertFilter {

    // 인증 실패 카운터 (IP 기준 카운트)
    private final Map<String, AtomicInteger> authFailureCounters = new ConcurrentHashMap<>();
    private static final int AUTH_FAILURE_THRESHOLD = 3;
    private static final long RESET_INTERVAL_MS = 600000; // 10분

    public boolean shouldAlert(Throwable ex, HttpServletRequest request) {
        // 5XX 서버 에러
        if (isServerError(ex)) {
            log.debug("Discord alert: Server error - {}", ex.getClass().getSimpleName());
            return true;
        }

        // 인증 실패 반복 체크
        if (ex instanceof AuthException) {
            String clientIp = getClientIp(request);

            if (isRepeatedAuthFailure(clientIp)) {
                log.warn("Discord alert: Repeated auth failure from IP: {}", clientIp);
                return true;
            }
        }

        // 외부 API 장애
        if (isExternalApiFailure(ex)) {
            log.debug("Discord alert: External API failure - {}", ex.getClass().getSimpleName());
            return true;
        }

        if (ex instanceof DataAccessException) {
            log.error("Discord alert: Database connection failure");
            return true;
        }

        if (ex instanceof FileHandleException) {
            log.debug("Discord alert: File handling failure");
            return true;
        }

        return false;
    }

    private boolean isServerError(Throwable ex) {

        if (ex instanceof JwtException || ex instanceof IOException) {
            return true;
        }

        return ex instanceof RuntimeException &&
                !(
                        ex instanceof BadRequestException ||
                                ex instanceof ResourceNotFoundException ||
                                ex instanceof CursorException ||
                                ex instanceof PolicyException
                );
    }

    private boolean isExternalApiFailure(Throwable ex) {

        return ex instanceof ResourceAccessException ||
                (ex.getCause() != null && ex.getCause() instanceof IOException) ||
                (
                        ex.getMessage() != null &&
                                (ex.getMessage().contains("External API") || ex.getMessage().contains("외부 API"))
                );
    }

    private boolean isRepeatedAuthFailure(String clientIp) {
        AtomicInteger counter =
                authFailureCounters.computeIfAbsent(
                        clientIp,
                        k -> new AtomicInteger(0)
                );

        int count = counter.incrementAndGet();

        if (count == 1) {
            scheduleCounterReset(clientIp);
        }

        return count >= AUTH_FAILURE_THRESHOLD;
    }

    private void scheduleCounterReset(String clientIp) {
        new Thread(
                () -> {
                    try {
                        Thread.sleep(RESET_INTERVAL_MS);

                        authFailureCounters.remove(clientIp);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .start();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}