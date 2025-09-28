package com.mople.global.event.data.notify.handler.impl.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.comment.CommentReplyEventData;
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

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_PLAN;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_REVIEW;
import static com.mople.global.enums.NotifyType.COMMENT_REPLY;

@Component
@RequiredArgsConstructor
public class CommentReplyNotifyHandler implements NotifyHandler<CommentReplyEventData> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    @Override
    public NotifyType getType() {
        return COMMENT_REPLY;
    }

    @Override
    public Class<CommentReplyEventData> getHandledType() {
        return CommentReplyEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(CommentReplyEventData data, NotificationEvent notify) {
        return requestFactory.getCommentReplyPushToken(data.getSenderId(), data.getParentCommentId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(CommentReplyEventData data, NotificationEvent notify, List<User> users) {
        if (data.getPlanId() != null && data.getReviewId() == null) {
            MeetPlan plan = planRepository.findById(data.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_PLAN));

            return users.stream()
                    .map(u ->
                            Notification.builder()
                                    .type(getType())
                                    .action(COMPLETE)
                                    .meetId(plan.getMeet().getId())
                                    .planId(plan.getId())
                                    .payload(notify.payload())
                                    .user(u)
                                    .build()
                    )
                    .toList();
        }

        PlanReview review = reviewRepository.findReview(data.getReviewId())
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
