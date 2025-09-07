package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.entity.event.OutboxEvent;
import com.mople.entity.event.ProcessedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.global.event.handler.domain.DomainHandlerRegistry;
import com.mople.outbox.repository.OutboxEventRepository;
import com.mople.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final DomainHandlerRegistry registry;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(OutboxEvent event) throws JsonProcessingException {
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
        outboxEventRepository.eventPublished(event.getEventId());
    }

    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void handleSafely(DomainEventHandler<T> handler, DomainEvent event) {
        handler.handle((T) event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String eventId, String errorMessage) {
        outboxEventRepository.eventFailed(eventId, errorMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(String eventId, String errorMessage, int retrySec, int maxAttempts) {
        outboxEventRepository.eventRetry(eventId, errorMessage, retrySec, maxAttempts);
    }
}
