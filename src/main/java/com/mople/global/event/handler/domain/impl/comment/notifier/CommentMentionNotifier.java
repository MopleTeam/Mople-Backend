package com.mople.global.event.handler.domain.impl.comment.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.comment.CommentCreatedEvent;
import com.mople.dto.event.data.domain.global.NotifyRequestedEvent;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.repository.NotificationRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.POST;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class CommentMentionNotifier implements DomainEventHandler<CommentCreatedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<CommentCreatedEvent> getHandledType() {
        return CommentCreatedEvent.class;
    }

    @Override
    public void handle(CommentCreatedEvent event) {
        if (!event.isExistMention()) {
            return;
        }

        List<Long> targetIds = userReader.findCreatedMentionedUsers(event.commentWriterId(), event.commentId());

        if (targetIds.isEmpty()) {
            return;
        }

        User user = userRepository.findByIdAndStatus(event.commentWriterId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_USER));

        if (planRepository.existsByIdAndStatus(event.postId(), Status.ACTIVE)) {
            MeetPlan plan = planRepository.findByIdAndStatus(event.postId(), Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_PLAN));

            Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_MEET));

            CommentMentionNotifyEvent notifyEvent = CommentMentionNotifyEvent.builder()
                    .meetName(meet.getName())
                    .postId(event.postId())
                    .senderNickname(user.getNickname())
                    .build();

            List<Long> notificationIds = notificationRepository.saveAll(
                            targetIds.stream()
                                    .map(targetId ->
                                            Notification.builder()
                                                    .type(notifyEvent.notifyType())
                                                    .meetId(meet.getId())
                                                    .planId(plan.getId())
                                                    .payload(notifyEvent.payload())
                                                    .userId(targetId)
                                                    .build()
                                    )
                                    .toList()
                    ).stream()
                    .map(Notification::getId).toList();

            outboxService.save(
                    NOTIFY_REQUESTED,
                    POST,
                    plan.getId(),
                    new NotifyRequestedEvent(notifyEvent, notificationIds)
            );
            return;
        }

        PlanReview review = reviewRepository.findByPlanIdAndStatus(event.postId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_REVIEW));

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_MEET));

        CommentMentionNotifyEvent notifyEvent = CommentMentionNotifyEvent.builder()
                .meetName(meet.getName())
                .postId(event.postId())
                .senderNickname(user.getNickname())
                .build();

        List<Long> notificationIds = notificationRepository.saveAll(
                        targetIds.stream()
                                .map(targetId ->
                                        Notification.builder()
                                                .type(notifyEvent.notifyType())
                                                .meetId(meet.getId())
                                                .reviewId(review.getId())
                                                .payload(notifyEvent.payload())
                                                .userId(targetId)
                                                .build()
                                )
                                .toList()
                ).stream()
                .map(Notification::getId).toList();

        outboxService.save(
                NOTIFY_REQUESTED,
                POST,
                review.getPlanId(),
                new NotifyRequestedEvent(notifyEvent, notificationIds)
        );
    }
}
