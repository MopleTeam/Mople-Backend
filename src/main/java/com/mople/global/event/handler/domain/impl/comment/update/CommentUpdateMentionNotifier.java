package com.mople.global.event.handler.domain.impl.comment.update;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.comment.CommentMentionAddedEvent;
import com.mople.dto.event.data.domain.global.NotifyRequestedEvent;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.global.event.service.PostContextFinder;
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
public class CommentUpdateMentionNotifier implements DomainEventHandler<CommentMentionAddedEvent> {

    private final PostContextFinder contextFinder;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<CommentMentionAddedEvent> getHandledType() {
        return CommentMentionAddedEvent.class;
    }

    @Override
    public void handle(CommentMentionAddedEvent event) {
        PostContextFinder.PostContext postContext = contextFinder.resolve(event.postId());
        List<Long> targetIds = userReader.findUpdatedMentionedUsers(
                event.originMentions(), event.commentWriterId(), event.commentId(), postContext.getMeet().getId()
        );

        if (targetIds.isEmpty()) {
            return;
        }

        User sender = userRepository.findByIdAndStatus(event.commentWriterId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_USER));

        CommentMentionNotifyEvent.CommentMentionNotifyEventBuilder eventBuilder = CommentMentionNotifyEvent.builder()
                .meetName(postContext.getMeet().getName())
                .senderNickname(sender.getNickname());

        if (postContext.isPlan()) {
            eventBuilder.planId(postContext.getPlanId());
        }
        if (postContext.isReview()) {
            eventBuilder.reviewId(postContext.getReviewId());
        }

        CommentMentionNotifyEvent notifyEvent = eventBuilder.build();

        List<Notification> notifications = targetIds.stream()
                .map(targetId -> {
                    Notification.NotificationBuilder notificationBuilder = Notification.builder()
                            .type(notifyEvent.notifyType())
                            .meetId(postContext.getMeet().getId())
                            .payload(notifyEvent.payload())
                            .userId(targetId);

                    if (postContext.isPlan()) {
                        notificationBuilder.planId(postContext.getPlanId());
                    }
                    if (postContext.isReview()) {
                        notificationBuilder.reviewId(postContext.getReviewId());
                    }

                    return notificationBuilder.build();
                })
                .toList();

        List<Long> notificationIds = notificationRepository.saveAll(notifications)
                .stream().map(Notification::getId).toList();

        outboxService.save(
                NOTIFY_REQUESTED,
                POST,
                postContext.getPlanId(),
                new NotifyRequestedEvent(notifyEvent, notificationIds)
        );
    }
}
