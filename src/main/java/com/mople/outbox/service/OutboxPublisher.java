package com.mople.outbox.service;

import com.mople.entity.event.OutboxEvent;
import com.mople.global.logging.logger.BusinessLogicLogger;
import com.mople.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxProcessor processor;
    private final OutboxEventRepository outboxEventRepository;
    private final BusinessLogicLogger logicLogger;
    private final Executor outboxExecutor;

    @Value("${outbox.batch-size}")
    private int batchSize;

    @Value("${outbox.lease-sec}")
    private int leaseSec;

    @Scheduled(fixedDelayString = "${outbox.fixed-delay-ms}")
    public void publishBatch() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.lockNextBatch(batchSize, leaseSec);

        List<CompletableFuture<Void>> futures = outboxEvents.stream()
                .map(event -> CompletableFuture.runAsync(
                        () -> processor.processOne(event),
                        outboxExecutor
                ).exceptionally(ex -> {
                    logicLogger.logError("OutboxEvent 처리 실패");
                    return null;
                }))
                .toList();

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .join();
    }
}
