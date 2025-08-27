package com.mople.outbox.service;

import com.mople.entity.event.OutboxEvent;
import com.mople.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxProcessor processor;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${outbox.batch-size}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.fixed-delay-ms}")
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.lockNextBatch(batchSize);

        for (OutboxEvent event : outboxEvents) {
            try {
                processor.processOne(event);
            } catch (Exception ex) {
                event.occurredError(ex);
            }
        }
    }
}
