package com.groupMeeting.notification.service;

import com.google.firebase.messaging.*;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.event.data.EventData;
import com.groupMeeting.dto.event.data.comment.CommentEventData;
import com.groupMeeting.dto.event.data.comment.impl.CommentMentionEventData;
import com.groupMeeting.dto.event.data.meet.MeetEventData;
import com.groupMeeting.dto.event.data.plan.PlanEventData;
import com.groupMeeting.dto.event.data.review.ReviewEventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.review.PlanReview;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Action;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;
import com.groupMeeting.notification.repository.NotificationRepository;

import com.groupMeeting.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.groupMeeting.global.enums.Action.PENDING;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_PLAN;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_REVIEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {
    private final FirebaseMessaging sender;
    private final NotificationRepository notificationRepository;
    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReviewRepository reviewRepository;

    @Transactional
    public void sendMultiNotification(NotificationEvent notify, NotifyType type, EventData data) {

        Long id;

        NotifySendRequest sendRequest =
                switch (type) {
                    case MEET_NEW_MEMBER, PLAN_CREATE -> {
                        MeetEventData meetData = (MeetEventData) data;
                        id = meetData.getMeetId();

                        yield requestFactory.getMeetPushToken(meetData.triggeredBy(), id, notify.topic());
                    }

                    case PLAN_UPDATE, PLAN_DELETE -> {
                        PlanEventData planData = (PlanEventData) data;
                        id = planData.getPlanId();

                        yield requestFactory.getPlanPushToken(planData.triggeredBy(), id, notify.topic());
                    }

                    case PLAN_REMIND -> {
                        PlanEventData planData = (PlanEventData) data;
                        id = planData.getPlanId();

                        yield requestFactory.getPlanRemindToken(id, notify.topic());
                    }

                    case REVIEW_REMIND -> {
                        ReviewEventData reviewData = (ReviewEventData) data;
                        id = reviewData.getReviewId();

                        yield requestFactory.getReviewCreatorPushToken(reviewData.triggeredBy(), notify.topic());
                    }

                    case REVIEW_UPDATE -> {
                        ReviewEventData reviewData = (ReviewEventData) data;
                        id = reviewData.getReviewId();

                        yield requestFactory.getReviewPushToken(reviewData.triggeredBy(), id, notify.topic());
                    }

                    case COMMENT_REPLY -> {
                        CommentEventData commentData = (CommentEventData) data;
                        id = commentData.getCommentId();

                        yield requestFactory.getCommentReplyPushToken(commentData.triggeredBy(), commentData.getParentId(), notify.topic());
                    }

                    case COMMENT_MENTION -> {
                        CommentMentionEventData mentionData = (CommentMentionEventData) data;
                        id = mentionData.getCommentId();

                        yield requestFactory.getCommentMentionPushToken(mentionData.getOriginMentions(), mentionData.getSenderId(), id, notify.topic());
                    }
                };

        if (!sendRequest.tokens().isEmpty()) {
            sender.sendEachAsync(
                    sendRequest
                            .tokens()
                            .stream()
                            .map(token -> {
                                User findUser = sendRequest.findUserByToken(token);

                                return Message
                                        .builder()
                                        .setNotification(
                                                com.google.firebase.messaging.Notification
                                                        .builder()
                                                        .setTitle(notify.payload().title())
                                                        .setBody(notify.payload().message())
                                                        .build()
                                        )
                                        .putAllData(notify.body())
                                        .setApnsConfig(
                                                ApnsConfig
                                                        .builder()
                                                        .setAps(Aps.builder()
                                                                .setSound("default")
                                                                .setBadge(findUser.getBadgeCount() + 1)
                                                                .build()
                                                        )
                                                        .build()
                                        )
                                        .setAndroidConfig(
                                                AndroidConfig.builder()
                                                        .setTtl(3600)
                                                        .setNotification(
                                                                AndroidNotification
                                                                        .builder()
                                                                        .setTitle(notify.payload().title())
                                                                        .setBody(notify.payload().message())
                                                                        .setDefaultSound(true)
                                                                        .setNotificationCount(findUser.getBadgeCount() + 1)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .setToken(sendRequest.validToken(token))
                                        .build();
                            })
                            .toList()
            );
        }

        sendRequest.users().forEach(User::updateBadgeCount);

        log.info("알림 전송 = {} {} {}", type.name(), notify.payload().title(), id);

        notificationRepository.saveAll(

                switch (type) {
                    case MEET_NEW_MEMBER -> {
                        MeetEventData meetData = (MeetEventData) data;

                        yield getMeetNotifications(type, notify, sendRequest.users(), meetData.getMeetId());
                    }

                    case PLAN_CREATE, PLAN_UPDATE -> {
                        PlanEventData planData = (PlanEventData) data;

                        yield getPlanNotifications(type, notify, sendRequest.users(), planData.getMeetId(), planData.getPlanId());
                    }

                    case PLAN_DELETE -> {
                        PlanEventData planData = (PlanEventData) data;

                        deletePlan(id);
                        yield getPlanNotifications(type, notify, sendRequest.users(), planData.getMeetId(), null);
                    }

                    case PLAN_REMIND -> {
                        PlanEventData planData = (PlanEventData) data;

                        List<Notification> planRemindNotification =
                                notificationRepository.findPlanRemindNotification(planData.getPlanId(), PENDING);

                        planRemindNotification.forEach(n -> n.updateNotification(notify, type));

                        yield planRemindNotification;
                    }

                    case REVIEW_REMIND, REVIEW_UPDATE -> {
                        ReviewEventData reviewData = (ReviewEventData) data;

                        yield getReviewNotifications(type, notify, sendRequest.users(), reviewData.getMeetId(), reviewData.getReviewId());
                    }

                    case COMMENT_REPLY, COMMENT_MENTION -> {
                        CommentEventData commentData = (CommentEventData) data;

                        yield getCommentNotifications(type, notify, sendRequest.users(), commentData.getPostId());
                    }
                }
        );
    }

    private void deletePlan(Long id) {

        MeetPlan plan = meetPlanRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_PLAN)
        );

        plan.getMeet().removePlan(plan);

        meetPlanRepository.delete(plan);
    }

    private List<Notification> getMeetNotifications(NotifyType type, NotificationEvent notify, List<User> users, Long meetId) {

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(type)
                                .action(Action.COMPLETE)
                                .meetId(meetId)
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }

    private List<Notification> getPlanNotifications(NotifyType type, NotificationEvent notify, List<User> users, Long meetId, Long planId) {

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(type)
                                .action(Action.COMPLETE)
                                .meetId(meetId)
                                .planId(planId)
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }

    private List<Notification> getReviewNotifications(NotifyType type, NotificationEvent notify, List<User> users, Long meetId, Long reviewId) {

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(type)
                                .action(Action.COMPLETE)
                                .meetId(meetId)
                                .reviewId(reviewId)
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }

    private List<Notification> getCommentNotifications(NotifyType type, NotificationEvent notify, List<User> users, Long postId) {

        Optional<MeetPlan> maybePlan = meetPlanRepository.findById(postId);

        if (maybePlan.isPresent()) {
            return users.stream()
                    .map(u ->
                            Notification.builder()
                                    .type(type)
                                    .action(Action.COMPLETE)
                                    .meetId(maybePlan.get().getMeet().getId())
                                    .planId(maybePlan.get().getId())
                                    .payload(notify.payload())
                                    .user(u)
                                    .build()
                    )
                    .toList();
        }

        PlanReview review = reviewRepository.findReviewByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(type)
                                .action(Action.COMPLETE)
                                .meetId(review.getMeet().getId())
                                .reviewId(review.getId())
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }
}
