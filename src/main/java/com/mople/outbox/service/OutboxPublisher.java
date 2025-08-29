package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.entity.event.OutboxEvent;
import com.mople.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxProcessor processor;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${outbox.batch-size}")
    private int batchSize;

    @Value("${outbox.lease-sec}")
    private int leaseSec;

    @Value("${outbox.retry-sec}")
    private int retrySec;

    @Value("${outbox.max-attempts}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${outbox.fixed-delay-ms}")
    public void publishBatch() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.lockNextBatch(batchSize, leaseSec);

        for (OutboxEvent event : outboxEvents) {
            try {
                processor.processOne(event);
            } catch (JsonProcessingException | NonRetryableOutboxException ex) {
                outboxEventRepository.eventFailed(event.getEventId(), shorten(ex.getMessage()));
            } catch (Exception ex) {
                outboxEventRepository.eventRetry(event.getEventId(), shorten(ex.getMessage()), retrySec, maxAttempts);
            }
        }
    }

    private String shorten(String s) {
        if (s == null) return null;
        return s.length() > 800 ? s.substring(0, 800) : s;
    }
}
