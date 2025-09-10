package com.mople.outbox.service;

import com.mople.outbox.repository.OutboxEventRepository;
import com.mople.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.IntUnaryOperator;

@Service
@RequiredArgsConstructor
public class OutboxCleanupJob {

    private final OutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Value("${outbox.cleanup.batch-size}")
    private int batchSize;

    @Value("${outbox.cleanup.keep-days.published}")
    private long keepPublishedDays;

    @Value("${outbox.cleanup.keep-days.canceled}")
    private long keepCanceledDays;

    @Value("${outbox.cleanup.keep-days.failed}")
    private long keepFailedDays;

    @Value("${outbox.cleanup.keep-days.processed}")
    private long keepProcessedDays;

    @Scheduled(cron = "${cron.outbox.cleanup}", zone = "Asia/Seoul")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime minusPublishedDays = now.minusDays(keepPublishedDays);
        job(i -> outboxEventRepository.deleteOldPublished(minusPublishedDays, batchSize));

        LocalDateTime minusCanceledDays = now.minusDays(keepCanceledDays);
        job(i -> outboxEventRepository.deleteOldCanceled(minusCanceledDays, batchSize));

        LocalDateTime minusFailedDays = now.minusDays(keepFailedDays);
        job(i -> outboxEventRepository.deleteOldFailed(minusFailedDays, batchSize));

        LocalDateTime minusProcessedDays = now.minusDays(keepProcessedDays);
        job(i -> processedEventRepository.deleteOldProcessed(minusProcessedDays, batchSize));
    }

    private void job(IntUnaryOperator fn) {
        int affected;
        do { affected = fn.applyAsInt(batchSize); }
        while (affected == batchSize);
    }
}
