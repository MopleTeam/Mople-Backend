package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.entity.event.OutboxEvent;
import com.mople.entity.event.ProcessedEvent;
import com.mople.global.event.handler.domain.DomainEventDispatcher;
import com.mople.outbox.repository.OutboxEventRepository;
import com.mople.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final DomainEventDispatcher dispatcher;
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
        dispatcher.dispatch(domainEvent);

        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        outboxEventRepository.eventPublished(event.getEventId());
    }
}
