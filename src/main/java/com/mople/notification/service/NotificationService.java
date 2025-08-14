package com.mople.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.request.notification.topic.PushTopicRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.notification.NotificationResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.entity.notification.Topic;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.PushTopic;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int NOTIFICATION_CURSOR_FIELD_COUNT = 1;

    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    private final EntityReader reader;

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
    public FlatCursorPageResponse<NotificationResponse> getUserNotificationList(Long userId, CursorPageRequest request) {
        reader.findUser(userId);

        int size = request.getSafeSize();
        List<NotificationResponse.NotificationListInterface> notifications = getNotifications(userId, request.cursor(), size);

        List<Long> planIds = notifications
                .stream()
                .map(NotificationResponse.NotificationListInterface::getPlanId)
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

        List<NotificationResponse> notificationListResponses = NotificationResponse.of(objectMapper, notifications, planMap);

        return FlatCursorPageResponse.of(
                notificationRepository.countBadgeCount(userId, Action.COMPLETE.name(), LocalDateTime.now().minusDays(30)),
                buildNotificationCursorPage(size, notificationListResponses)
        );
    }

    private List<NotificationResponse.NotificationListInterface> getNotifications(Long userId, String encodedCursor, int size) {
        int limit = size + 1;

        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return notificationRepository.findNotificationFirstPage(userId, Action.COMPLETE.name(), limit);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor, NOTIFICATION_CURSOR_FIELD_COUNT);
        Long cursorId = Long.valueOf(decodeParts[0]);

        validateCursor(cursorId);

        return notificationRepository.findNotificationNextPage(userId, Action.COMPLETE.name(), cursorId, limit);
    }

    private void validateCursor(Long cursorId) {
        if (notificationRepository.isCursorInvalid(cursorId).isEmpty()) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    private CursorPageResponse<NotificationResponse> buildNotificationCursorPage(int size, List<NotificationResponse> notificationListResponses) {
        return buildCursorPage(
                notificationListResponses,
                size,
                c -> new String[]{
                        c.notificationId().toString()
                },
                Function.identity()
        );
    }

    @Transactional
    public void readAllNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_USER));

        notificationRepository
                .getUserNotificationList(user.getId(), Action.COMPLETE)
                .forEach(Notification::updateReadAt);
    }

    @Transactional
    public void readSingleNotification(Long userId, Long notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_USER));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_NOTIFY));

        validateNotification(user.getId(), notification);

        notification.updateReadAt();
    }

    private void validateNotification(Long userId, Notification notification) {
        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException(NOT_OWNER_OF_NOTIFICATION);
        }
    }
}