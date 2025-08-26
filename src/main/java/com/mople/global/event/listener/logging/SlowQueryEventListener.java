package com.mople.global.event.listener.logging;

import com.mople.global.async.message.DiscordExceptionSender;
import com.mople.global.event.data.exception.DiscordField;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.DiscordMessagePayload;
import com.mople.global.event.data.logging.SlowQueryEvent;
import com.mople.global.logging.LoggingContextManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SlowQueryEventListener {

    private final DiscordExceptionSender discordSender;
    private final LoggingContextManager loggingContextManager;

    @EventListener
    public void handleSlowQueryEvent(SlowQueryEvent event) {
        var discordMessage = DiscordMessage
                .builder()
                .content("⚠️ **Slow Query Detected**")
                .embeds(
                        List.of(
                                DiscordMessagePayload.builder()
                                        .title("🐌 Slow Query")
                                        .color(16776960) // 노란색
                                        .fields(List.of(
                                                DiscordField.createField("Execute Time", event.executionTime() + "ms", true),
                                                DiscordField.createField("Request ID", loggingContextManager.getRequestId(), true),
                                                DiscordField.createField("Query", "```sql\n" + event.query() + "```", false)
                                        ))
                                        .build()
                        )
                )
                .build();

        discordSender.exceptionSend(discordMessage);
    }
}