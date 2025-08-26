package com.mople.meet.schedule;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.service.NotificationSendService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReviewReminderWorker {

    private final PlanReviewRepository reviewRepository;
    private final NotificationSendService sendService;

    @Transactional
    public void runReviewReminder(Long reviewId, String meetName) {
        PlanReview review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_REVIEW)
        );

        if (!review.getUpload()) {
            ReviewRemindNotifyEvent event = ReviewRemindNotifyEvent.builder()
                    .meetId(review.getMeetId())
                    .meetName(meetName)
                    .reviewId(review.getId())
                    .reviewName(review.getName())
                    .reviewCreatorId(review.getCreatorId())
                    .build();

            sendService.sendMultiNotification(event);
        }
    }
}
