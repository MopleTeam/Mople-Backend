package com.mople.outbox.service;

import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.global.enums.ExceptionReturnCode.ILLEGAL_EVENT;

@Service
@RequiredArgsConstructor
public class OutboxStateService {

    private final OutboxEventRepository outboxEventRepository;

    @Value("${outbox.retry-sec}")
    private int retrySec;

    @Value("${outbox.max-attempts}")
    private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(String eventId) {
        int updated = outboxEventRepository.eventPublished(eventId);
        if (updated != 1) {
            throw new IllegalStatesException(ILLEGAL_EVENT);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(String eventId, String errorMessage) {
        int updated = outboxEventRepository.eventRetry(eventId, errorMessage, retrySec, maxAttempts);
        if (updated != 1) {
            throw new IllegalStatesException(ILLEGAL_EVENT);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSkip(String eventId, String errorMessage) {
        int updated = outboxEventRepository.eventSkip(eventId, errorMessage);
        if (updated != 1) {
            throw new IllegalStatesException(ILLEGAL_EVENT);
        }
    }
}
