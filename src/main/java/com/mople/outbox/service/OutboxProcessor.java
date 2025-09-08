package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.entity.event.OutboxEvent;
import com.mople.entity.event.ProcessedEvent;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.global.event.handler.domain.DomainHandlerRegistry;
import com.mople.outbox.repository.OutboxEventRepository;
import com.mople.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.ILLEGAL_PROCESS_ORDER;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final DomainHandlerRegistry registry;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper mapper;

    @Value("${outbox.retry-sec}")
    private int retrySec;

    @Value("${outbox.max-attempts}")
    private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(OutboxEvent event) {
        try {
            if (processedEventRepository.existsById(event.getEventId())) {
                outboxEventRepository.eventPublished(event.getEventId());
                return;
            }

            DomainEvent domainEvent = mapper.readValue(event.getPayload(), DomainEvent.class);
            List<DomainEventHandler<? extends DomainEvent>> handlers = registry.getHandler(domainEvent);

            for (DomainEventHandler<? extends DomainEvent> handler : handlers) {
                handleSafely(handler, domainEvent);
            }

            processedEventRepository.save(new ProcessedEvent(event.getEventId()));

            int updated = outboxEventRepository.eventPublished(event.getEventId());
            if (updated != 1) {
                throw new IllegalStatesException(ILLEGAL_PROCESS_ORDER);
            }

        } catch (JsonProcessingException | NonRetryableOutboxException ex) {
            outboxEventRepository.eventFailed(event.getEventId(), shorten(ex.getMessage()));
        } catch (Exception ex) {
            outboxEventRepository.eventRetry(event.getEventId(), shorten(ex.getMessage()), retrySec, maxAttempts);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void handleSafely(DomainEventHandler<T> handler, DomainEvent event) {
        handler.handle((T) event);
    }

    private String shorten(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
