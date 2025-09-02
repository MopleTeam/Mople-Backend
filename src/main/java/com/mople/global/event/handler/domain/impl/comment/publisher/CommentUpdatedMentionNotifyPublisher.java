package com.mople.global.event.handler.domain.impl.comment.publisher;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.comment.CommentUpdatedEvent;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentUpdatedMentionNotifyPublisher implements DomainEventHandler<CommentUpdatedEvent> {

    private final MeetRepository meetRepository;
    private final UserRepository userRepository;
    private final PlanCommentRepository commentRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<CommentUpdatedEvent> getHandledType() {
        return CommentUpdatedEvent.class;
    }

    @Override
    public void handle(CommentUpdatedEvent event) {
        if (event.getNewMentions() == null || !event.getNewMentions().isEmpty()) {
            return;
        }

        PlanComment comment = commentRepository.findById(event.getCommentId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_COMMENT));

        Meet meet = meetRepository.findById(event.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        User user = userRepository.findById(event.getSenderId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        CommentMentionNotifyEvent notifyEvent = CommentMentionNotifyEvent.builder()
                .postId(comment.getPostId())
                .meetName(meet.getName())
                .commentId(event.getCommentId())
                .commentContent(comment.getContent())
                .senderId(event.getSenderId())
                .senderNickname(user.getNickname())
                .originMentions(event.getOriginMentions())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
