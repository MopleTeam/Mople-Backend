package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.review.ReviewCreatedEvent;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.REVIEW_REMIND;

@Component
@RequiredArgsConstructor
public class ReviewRemindRegisterHandler implements DomainEventHandler<ReviewCreatedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<ReviewCreatedEvent> supports() {
        return ReviewCreatedEvent.class;
    }

    @Override
    public void handle(ReviewCreatedEvent event) {

        LocalDateTime runAt = LocalDateTime
                .of(LocalDate.now(), LocalTime.of(12, 0, 0))
                .atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        ReviewRemindEvent remindEvent = ReviewRemindEvent.builder()
                .reviewId(event.getReviewId())
                .build();

        outboxService.saveWithRunAt(REVIEW_REMIND, REVIEW, event.getReviewId(), runAt, remindEvent);
    }
}
