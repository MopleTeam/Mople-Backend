package com.groupMeeting.global.event.data.notify.handler.impl.comment;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.event.data.comment.CommentMentionEventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.review.PlanReview;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandler;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;
import com.groupMeeting.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.groupMeeting.global.enums.Action.COMPLETE;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_REVIEW;
import static com.groupMeeting.global.enums.NotifyType.COMMENT_MENTION;

@Component
@RequiredArgsConstructor
public class CommentMentionNotifyHandler implements NotifyHandler<CommentMentionEventData> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    @Override
    public NotifyType getType() {
        return COMMENT_MENTION;
    }

    @Override
    public Class<CommentMentionEventData> getHandledType() {
        return CommentMentionEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(CommentMentionEventData data, NotificationEvent notify) {
        return requestFactory.getCommentMentionPushToken(data.getOriginMentions(), data.getSenderId(), data.getCommentId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(CommentMentionEventData data, NotificationEvent notify, List<User> users) {
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
