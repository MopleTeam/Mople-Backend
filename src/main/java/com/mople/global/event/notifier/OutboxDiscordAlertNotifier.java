package com.mople.global.event.notifier;

import com.mople.entity.event.OutboxEvent;
import com.mople.global.async.message.DiscordExceptionSender;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.DiscordMessagePayload;
import com.mople.global.event.filter.OutboxDiscordAlertFilter;
import com.mople.global.event.notifier.support.StackTracePreviewer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class OutboxDiscordAlertNotifier {

    private final DiscordExceptionSender exceptionSender;
    private final OutboxDiscordAlertFilter outboxAlertFilter;
    private final StackTracePreviewer stackTracePreviewer;

    public void notifyOutboxFailure(String title, OutboxEvent event, Throwable ex) {
        Throwable root = unwrap(ex);

        if (!outboxAlertFilter.shouldAlert(root, event)) {
            return;
        }

        String message = """
                EventId: %s
                Status: %s
                Attempts: %s
                Type: %s
                exception: %s(message=%s)
                Stacktrace: %s
                """
                .formatted(
                        safe(event.getEventId()),
                        safe(event.getStatus()),
                        safe(event.getAttempts()),
                        safe(event.getEventType()),
                        root.getClass().getName(),
                        root.getMessage(),
                        stackTracePreviewer.preview(root)
                );

        DiscordMessage discordMessage = DiscordMessage.builder()
                .content("# 🚨 Outbox Exception")
                .embeds(List.of(
                        DiscordMessagePayload.builder()
                                .title("ℹ️ " + title)
                                .description(message)
                                .build()
                ))
                .build();

        exceptionSender.exceptionSend(discordMessage);
    }

    public void notifyOutboxBatchFailure(String title, Throwable ex) {
        Throwable root = unwrap(ex);

        String message = """
                exception: %s(message=%s)
                Stacktrace: %s
                """
                .formatted(
                        root.getClass().getName(),
                        root.getMessage(),
                        stackTracePreviewer.preview(root)
                );

        DiscordMessage discordMessage = DiscordMessage.builder()
                .content("# 🚨 Outbox Batch Exception")
                .embeds(List.of(
                        DiscordMessagePayload.builder()
                                .title("ℹ️ " + title)
                                .description(message)
                                .build()
                ))
                .build();

        exceptionSender.exceptionSend(discordMessage);
    }

    private Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return ex.getCause();
        }

        if (ex instanceof ExecutionException && ex.getCause() != null) {
            return ex.getCause();
        }

        return ex;
    }

    private String safe(Object o) {
        return String.valueOf(o);
    }
}
