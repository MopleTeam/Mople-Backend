package com.mople.global.event.handler.domain.impl.comment.create;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.comment.CommentCreatedEvent;
import com.mople.dto.event.data.domain.global.NotifyRequestedEvent;
import com.mople.dto.event.data.notify.comment.CommentReplyNotifyEvent;
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
public class CommentReplyNotifier implements DomainEventHandler<CommentCreatedEvent> {

    private final PostContextFinder contextFinder;
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
        if (event.parentId() == null) {
            return;
        }

        PostContextFinder.PostContext postContext = contextFinder.resolve(event.postId());
        Long targetId = userReader.findCommentRepliedUserNoWriter(
                event.commentWriterId(),
                event.parentId(),
                postContext.getMeet().getId()
        );

        if (targetId == null) {
            return;
        }

        User sender = userRepository.findByIdAndStatus(event.commentWriterId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_USER));

        if (event.isExistMention()) {
            List<Long> mentionedUsers = userReader.findCreatedMentionedUsers(
                    event.commentWriterId(),
                    event.commentId(),
                    postContext.getMeet().getId()
            );

            if (!mentionedUsers.isEmpty() && mentionedUsers.contains(targetId)) {
                return;
            }
        }

        CommentReplyNotifyEvent.CommentReplyNotifyEventBuilder eventBuilder = CommentReplyNotifyEvent.builder()
                .meetName(postContext.getMeet().getName())
                .senderNickname(sender.getNickname());

        if (postContext.isPlan()) {
            eventBuilder.planId(postContext.getPlanId());
        }
        if (postContext.isReview()) {
            eventBuilder.reviewId(postContext.getReviewId());
        }

        CommentReplyNotifyEvent notifyEvent = eventBuilder.build();

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

        Notification notification = notificationBuilder.build();

        Long notificationId = notificationRepository.save(notification).getId();

        outboxService.save(
                NOTIFY_REQUESTED,
                POST,
                postContext.getPlanId(),
                new NotifyRequestedEvent(notifyEvent, List.of(notificationId))
        );
    }
}
