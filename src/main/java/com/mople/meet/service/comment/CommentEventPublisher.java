package com.mople.meet.service.comment;

import com.mople.dto.event.data.comment.CommentMentionEventData;
import com.mople.dto.event.data.comment.CommentReplyEventData;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
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
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    public void publishMentionEvent(List<Long> originMentions, List<Long> newMentions, PlanComment comment, String meetName) {
        if (newMentions == null || newMentions.isEmpty()) return;

        publisher.publishEvent(
                NotifyEventPublisher.commentMention(
                        CommentMentionEventData.builder()
                                .planId(getPlanId(comment.getPostId()))
                                .reviewId(getReviewId(comment.getPostId()))                                .meetName(meetName)
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
                            CommentReplyEventData.builder()
                                    .planId(getPlanId(comment.getPostId()))
                                    .reviewId(getReviewId(comment.getPostId()))
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

    private Long getPlanId(Long postId) {
        if (planRepository.existsById(postId)) {
            return postId;
        }
        return null;
    }

    private Long getReviewId(Long postId) {
        if (reviewRepository.existsByPlanId(postId)) {
            return postId;
        }
        return null;
    }
}
