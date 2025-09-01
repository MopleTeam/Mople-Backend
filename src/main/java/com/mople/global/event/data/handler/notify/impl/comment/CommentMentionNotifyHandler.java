package com.mople.global.event.data.handler.notify.impl.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.handler.notify.NotifyEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_REVIEW;

@Component
@RequiredArgsConstructor
public class CommentMentionNotifyHandler implements NotifyEventHandler<CommentMentionNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    @Override
    public Class<CommentMentionNotifyEvent> getHandledType() {
        return CommentMentionNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(CommentMentionNotifyEvent event) {
        return requestFactory.getCommentMentionPushToken(event.getOriginMentions(), event.getSenderId(), event.getCommentId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(CommentMentionNotifyEvent event, List<User> users) {
        Optional<MeetPlan> plan = planRepository.findById(event.getPostId());

        if (plan.isPresent()) {
            return users.stream()
                    .map(u ->
                            Notification.builder()
                                    .type(event.notifyType())
                                    .action(COMPLETE)
                                    .meetId(plan.get().getMeetId())
                                    .planId(plan.get().getId())
                                    .payload(event.payload())
                                    .userId(u.getId())
                                    .build()
                    )
                    .toList();
        }

        PlanReview review = reviewRepository.findReviewByPostId(event.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(event.notifyType())
                                .action(COMPLETE)
                                .meetId(review.getMeetId())
                                .reviewId(review.getId())
                                .payload(event.payload())
                                .userId(u.getId())
                                .build()
                )
                .toList();
    }
}
