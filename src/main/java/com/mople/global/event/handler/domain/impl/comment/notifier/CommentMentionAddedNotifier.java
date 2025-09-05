package com.mople.global.event.handler.domain.impl.comment.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.domain.comment.CommentMentionAddedEvent;
import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.service.NotificationSendService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentMentionAddedNotifier implements DomainEventHandler<CommentMentionAddedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<CommentMentionAddedEvent> getHandledType() {
        return CommentMentionAddedEvent.class;
    }

    @Override
    public void handle(CommentMentionAddedEvent event) {
        List<Long> filteredTargetIds = userReader.findUpdatedMentionedUsers(
                event.getOriginMentions(), event.getCommentWriterId(), event.getCommentId()
        );

        User user = userRepository.findByIdAndStatus(event.getCommentWriterId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        if (isPlan(event.getPostId())) {
            MeetPlan plan = planRepository.findByIdAndStatus(event.getPostId(), Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_PLAN));

            Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_MEET));

            CommentMentionNotifyEvent notifyEvent = CommentMentionNotifyEvent.builder()
                    .meetId(meet.getId())
                    .meetName(meet.getName())
                    .postId(event.getPostId())
                    .planId(plan.getId())
                    .reviewId(null)
                    .senderNickname(user.getNickname())
                    .targetIds(filteredTargetIds)
                    .build();

            sendService.sendMultiNotification(notifyEvent);
            return;
        }

        PlanReview review = reviewRepository.findByPlanIdAndStatus(event.getPostId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_REVIEW));

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_MEET));

        CommentMentionNotifyEvent notifyEvent = CommentMentionNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .postId(event.getPostId())
                .planId(null)
                .reviewId(review.getId())
                .senderNickname(user.getNickname())
                .targetIds(filteredTargetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }

    private boolean isPlan(Long postId) {
        try {
            planRepository.findByIdAndStatus(postId, Status.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.INVALID_PLAN));

            return true;
        } catch (ResourceNotFoundException e) {
            reviewRepository.findByPlanIdAndStatus(postId, Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.INVALID_REVIEW));

            return false;
        }
    }
}
