package com.mople.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.request.notification.topic.PushTopicRequest;
import com.mople.dto.response.notification.NotificationListResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.entity.notification.Topic;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.PushTopic;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.repository.TopicRepository;
import com.mople.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mople.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<PushTopic> getSubscribeList(Long userId) {

        return topicRepository
                .findAllUserIdTopic(userId)
                .stream()
                .map(Topic::getTopic)
                .toList();
    }

    @Transactional
    public void subscribeNotifyTopic(Long userId, PushTopicRequest request) {
        List<Topic> userTopic = topicRepository.findAllUserIdTopic(userId);

        if (userTopic.isEmpty()) {
            List<Topic> topics =
                    request.topics()
                            .stream()
                            .map(topic -> new Topic(userId, topic))
                            .toList();

            topicRepository.saveAll(topics);
            return;
        }

        List<PushTopic> topics = userTopic.stream().map(Topic::getTopic).toList();

        request.topics()
                .stream()
                .filter(r -> !topics.contains(r))
                .forEach(r -> topicRepository.save(new Topic(userId, r)));
    }

    @Transactional
    public void unsubscribeNotifyTopic(Long userId, PushTopicRequest request) {
        List<Topic> userTopic = topicRepository.findAllUserTopic(userId, request.topics());

        if (userTopic.isEmpty()) {
            return;
        }

        userTopic.forEach(topic -> topicRepository.deleteById(topic.getId()));
    }

    @Transactional(readOnly = true)
    public List<NotificationListResponse> getUserNotificationList(Long userId) {

        List<Long> planIds = notificationRepository
                .getUserNotificationListLimit(userId, Action.COMPLETE)
                .stream()
                .map(NotificationListResponse.NotificationListInterface::getPlanId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, LocalDateTime> planMap = planRepository.findPlanAndTime(planIds)
                .stream()
                .collect(Collectors.toMap(
                                MeetPlan::getId,
                                MeetPlan::getPlanTime
                        )
                );

        planMap.putAll(
                reviewRepository.findReviewsByPostId(planIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PlanReview::getPlanId,
                                        PlanReview::getPlanTime
                                )
                        )
        );

        return NotificationListResponse.of(
                objectMapper,
                notificationRepository
                        .getUserNotificationListLimit(userId, Action.COMPLETE),
                planMap
        );
    }

    @Transactional
    public void userBadgeCountClear(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_USER));

        user.clearBadgeCount();

        notificationRepository
                .getUserNotificationList(userId, Action.COMPLETE)
                .forEach(Notification::updateReadAt);
    }

    @Transactional
    public void userBadgeCountMinus(Long userId, Long notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_USER));

        user.minusBadgeCount();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTIFY));

        notification.updateReadAt();
    }
}