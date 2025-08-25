package com.mople.global.event.data.notify.handler.impl.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_REVIEW;
import static com.mople.global.enums.NotifyType.COMMENT_MENTION;

@Component
@RequiredArgsConstructor
public class CommentMentionNotifyHandler implements NotifyHandler<CommentMentionNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    @Override
    public NotifyType getType() {
        return COMMENT_MENTION;
    }

    @Override
    public Class<CommentMentionNotifyEvent> getHandledType() {
        return CommentMentionNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(CommentMentionNotifyEvent data, NotificationEvent notify) {
        return requestFactory.getCommentMentionPushToken(data.getOriginMentions(), data.getSenderId(), data.getCommentId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(CommentMentionNotifyEvent data, NotificationEvent notify, List<User> users) {
        Optional<MeetPlan> plan = planRepository.findById(data.getPostId());

        if (plan.isPresent()) {
            return users.stream()
                    .map(u ->
                            Notification.builder()
                                    .type(getType())
                                    .action(COMPLETE)
                                    .meetId(plan.get().getMeet().getId())
                                    .planId(plan.get().getId())
                                    .payload(notify.payload())
                                    .user(u)
                                    .build()
                    )
                    .toList();
        }

        PlanReview review = reviewRepository.findReviewByPostId(data.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(getType())
                                .action(COMPLETE)
                                .meetId(review.getMeet().getId())
                                .reviewId(review.getId())
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }
}
