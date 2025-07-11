package com.groupMeeting.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.request.notification.topic.PushTopicRequest;
import com.groupMeeting.dto.response.notification.NotificationListResponse;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.notification.Topic;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Action;
import com.groupMeeting.global.enums.PushTopic;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.notification.repository.NotificationRepository;
import com.groupMeeting.notification.repository.TopicRepository;
import com.groupMeeting.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MeetPlanRepository planRepository;

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

        return NotificationListResponse.of(
                objectMapper,
                notificationRepository
                        .getUserNotificationListLimit(userId, Action.COMPLETE),
                planRepository.findPlanAndTime(planIds)
                        .stream()
                        .collect(Collectors.toMap(
                                        MeetPlan::getId,
                                        MeetPlan::getPlanTime
                                )
                        )
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