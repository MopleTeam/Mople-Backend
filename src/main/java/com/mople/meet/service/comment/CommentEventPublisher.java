package com.mople.meet.service.comment;

import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.dto.event.data.notify.comment.CommentReplyNotifyEvent;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.notification.reader.NotificationUserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final ApplicationEventPublisher publisher;
    private final NotificationUserReader userReader;

    public void publishMentionEvent(List<Long> originMentions, List<Long> newMentions, PlanComment comment, String meetName) {
        if (newMentions == null || newMentions.isEmpty()) return;

        publisher.publishEvent(
                NotifyEventPublisher.commentMention(
                        CommentMentionNotifyEvent.builder()
                                .postId(comment.getPostId())
                                .meetName(meetName)
                                .commentId(comment.getId())
                                .commentContent(comment.getContent())
                                .senderId(comment.getWriter().getId())
                                .senderNickname(comment.getWriter().getNickname())
                                .originMentions(originMentions)
                                .build()
                )
        );
    }

    public void publishReplyEvent(List<Long> mentions, PlanComment comment, PlanComment parentComment, String meetName) {
        boolean parentIsMentioned = false;

        if (mentions != null && !mentions.isEmpty()) {
            List<User> mentionedUsers = userReader.findMentionedUsers(comment.getWriter().getId(), comment.getId());
            User parentCommentWriter = parentComment.getWriter();

            parentIsMentioned = mentionedUsers.stream().anyMatch(user -> user.getId().equals(parentCommentWriter.getId()));
        }

        if (!parentIsMentioned) {
            publisher.publishEvent(
                    NotifyEventPublisher.commentReply(
                            CommentReplyNotifyEvent.builder()
                                    .postId(comment.getPostId())
                                    .meetName(meetName)
                                    .commentId(comment.getId())
                                    .commentContent(comment.getContent())
                                    .senderId(comment.getWriter().getId())
                                    .senderNickname(comment.getWriter().getNickname())
                                    .parentCommentId(comment.getParentId())
                                    .build()
                    )
            );
        }
    }
}
