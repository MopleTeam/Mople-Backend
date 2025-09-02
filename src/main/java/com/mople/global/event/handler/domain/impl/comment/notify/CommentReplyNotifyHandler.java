package com.mople.global.event.handler.domain.impl.comment.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.comment.CommentCreatedEvent;
import com.mople.dto.event.data.notify.comment.CommentReplyNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.service.NotificationSendService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentReplyNotifyHandler implements DomainEventHandler<CommentCreatedEvent> {

    private final MeetRepository meetRepository;
    private final UserRepository userRepository;
    private final PlanCommentRepository commentRepository;
    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<CommentCreatedEvent> supports() {
        return CommentCreatedEvent.class;
    }

    @Override
    public void handle(CommentCreatedEvent event) {
        if (event.getParentId() == null) {
            return;
        }

        PlanComment comment = commentRepository.findById(event.getCommentId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_COMMENT));

        Meet meet = meetRepository.findById(event.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        User user = userRepository.findById(event.getSenderId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        PlanComment parentComment = commentRepository.findById(event.getParentId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_COMMENT));

        if (event.getMentions() != null && !event.getMentions().isEmpty()) {
            List<User> mentionedUsers = userReader.findMentionedUsers(event.getSenderId(), event.getCommentId());
            Long parentCommentWriterId = parentComment.getWriterId();

            boolean parentIsMentioned = mentionedUsers.stream().anyMatch(u -> u.getId().equals(parentCommentWriterId));

            if (parentIsMentioned) {
                return;
            }
        }

        CommentReplyNotifyEvent notifyEvent = CommentReplyNotifyEvent.builder()
                .postId(comment.getPostId())
                .meetName(meet.getName())
                .commentId(event.getCommentId())
                .commentContent(comment.getContent())
                .senderId(event.getSenderId())
                .senderNickname(user.getNickname())
                .parentCommentId(event.getParentId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
