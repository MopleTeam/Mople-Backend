package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.core.exception.custom.AsyncException;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.entity.event.OutboxEvent;
import com.mople.global.enums.event.AggregateType;
import com.mople.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.mople.global.enums.ExceptionReturnCode.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper mapper;

    @Transactional
    public int cancel(String eventType, AggregateType aggregateType, Long aggregateId) {
        return eventRepository.eventCanceled(eventType, aggregateType, aggregateId);
    }

    @Transactional
    public String save(String eventType, AggregateType aggregateType, Long aggregateId, DomainEvent event) {
        try {
            String eventId = UUID.randomUUID().toString();
            eventRepository.save(
                    OutboxEvent.builder()
                            .eventId(eventId)
                            .eventType(eventType)
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .availableAt(LocalDateTime.now())
                            .payload(mapper.writeValueAsString(event))
                            .build()
            );

            return eventId;
        } catch (JsonProcessingException e) {
            throw new AsyncException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public String saveWithRunAt(String eventType, AggregateType aggregateType, Long aggregateId, LocalDateTime runAt, DomainEvent event) {
        try {
            String eventId = UUID.randomUUID().toString();
            eventRepository.save(
                    OutboxEvent.builder()
                            .eventId(eventId)
                            .eventType(eventType)
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .availableAt(runAt)
                            .payload(mapper.writeValueAsString(event))
                            .build()
            );

            return eventId;
        } catch (JsonProcessingException e) {
            throw new AsyncException(INTERNAL_SERVER_ERROR);
        }
    }
}
