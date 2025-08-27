package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.entity.event.OutboxEvent;
import com.mople.entity.event.ProcessedEvent;
import com.mople.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final ApplicationEventPublisher publisher;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(OutboxEvent event) throws JsonProcessingException {
        if (processedEventRepository.existsById(event.getEventId())) {
            event.published();
            return;
        }

        DomainEvent domainEvent = mapper.readValue(event.getPayload(), DomainEvent.class);
        publisher.publishEvent(domainEvent);

        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
        event.published();
    }
}
