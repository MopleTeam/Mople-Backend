package com.mople.global.event.filter;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.entity.event.OutboxEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OutboxDiscordAlertFilter {

    private final ConcurrentHashMap<String, Long> cooldown = new ConcurrentHashMap<>();
    private static final int ATTEMPT_THRESHOLD = 3;
    private static final long RESET_INTERVAL_MS = 10 * 60 * 1000; // 10분

    public boolean shouldAlert(Throwable ex, OutboxEvent event) {

        // 치명 예외
        if (ex instanceof NonRetryableOutboxException) {
            return passCooldown(event, ex);
        }

        // DB 계열
        if (ex instanceof DataAccessException) {
            return passCooldown(event, ex);
        }

        // 외부 API 장애
        if (isExternalApiFailure(ex)) {
            return passCooldown(event, ex);
        }

        // 일반 런타임 예외: attempts가 누적
        Integer attempts = event.getAttempts();
        if (attempts >= ATTEMPT_THRESHOLD) {
            return passCooldown(event, ex);
        }

        return false;
    }

    private boolean isExternalApiFailure(Throwable ex) {

        return ex instanceof ResourceAccessException ||
                (ex.getCause() != null && ex.getCause() instanceof IOException) ||
                (
                        ex.getMessage() != null &&
                                (ex.getMessage().contains("External API") || ex.getMessage().contains("외부 API"))
                );
    }

    private boolean passCooldown(OutboxEvent event, Throwable ex) {
        String key = event.getEventId() + "|" + ex.getClass().getName();
        long now = System.currentTimeMillis();

        Long last = cooldown.put(key, now);
        if (last == null) return true;

        if (now - last >= RESET_INTERVAL_MS) {
            cooldown.put(key, now);
            return true;
        }
        return false;
    }
}
