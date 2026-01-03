package com.mople.global.event.notifier;

import com.mople.global.async.message.DiscordExceptionSender;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.DiscordMessagePayload;
import com.mople.global.event.filter.HttpDiscordAlertFilter;
import com.mople.global.event.notifier.support.StackTracePreviewer;
import com.mople.global.logging.SensitiveLogger;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HttpDiscordAlertNotifier {

    private final DiscordExceptionSender exceptionSender;
    private final SensitiveLogger sensitiveLogger;
    private final HttpDiscordAlertFilter httpAlertFilter;
    private final StackTracePreviewer stackTracePreviewer;

    public void notify(JoinPoint point, HttpServletRequest request, Throwable ex) {
        if (!httpAlertFilter.shouldAlert(ex, request)) {
            return;
        }

        String message = """
                Request - Method: %s
                URI: %s
                Arguments: %s
                exception: %s(message=%s)
                Stacktrace: %s
                """
                .formatted(
                        request.getMethod(),
                        request.getRequestURI(),
                        sensitiveLogger.sensitiveArgs(point.getArgs()),
                        ex.getClass().getName(),
                        ex.getMessage(),
                        stackTracePreviewer.preview(ex)
                );

        DiscordMessage discordMessage = DiscordMessage.builder()
                .content("# 🚨 Server Critical Exception")
                .embeds(List.of(
                        DiscordMessagePayload.builder()
                                .title("ℹ️ Error Details")
                                .description(message)
                                .build()
                ))
                .build();

        exceptionSender.exceptionSend(discordMessage);
    }
}
