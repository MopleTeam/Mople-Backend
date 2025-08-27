package com.mople.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.core.exception.custom.AsyncException;
import com.mople.entity.event.OutboxEvent;
import com.mople.global.enums.AggregateType;
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

    private static final int DEFAULT_EVENT_VERSION = 1;

    private final OutboxEventRepository eventRepository;
    private final ObjectMapper mapper;

    @Transactional
    public String save(String eventType, AggregateType aggregateType, Long aggregateId, Object payload) {
        try {
            String eventId = UUID.randomUUID().toString();
            eventRepository.save(
                    OutboxEvent.builder()
                            .eventId(eventId)
                            .eventType(eventType)
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .eventVersion(DEFAULT_EVENT_VERSION)
                            .availableAt(LocalDateTime.now())
                            .payload(mapper.writeValueAsString(payload))
                            .build()
            );

            return eventId;
        } catch (JsonProcessingException e) {
            throw new AsyncException(INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public String saveWithRunAt(String eventType, AggregateType aggregateType, Long aggregateId, LocalDateTime runAt, Object payload) {
        try {
            String eventId = UUID.randomUUID().toString();
            eventRepository.save(
                    OutboxEvent.builder()
                            .eventId(eventId)
                            .eventType(eventType)
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .eventVersion(DEFAULT_EVENT_VERSION)
                            .availableAt(runAt)
                            .payload(mapper.writeValueAsString(payload))
                            .build()
            );

            return eventId;
        } catch (JsonProcessingException e) {
            throw new AsyncException(INTERNAL_SERVER_ERROR);
        }
    }
}
