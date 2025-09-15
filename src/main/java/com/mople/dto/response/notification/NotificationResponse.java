package com.mople.dto.response.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.global.enums.event.NotifyType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public record NotificationResponse(
        Long notificationId,
        Long meetId,
        Long planId,
        Long reviewId,
        String meetName,
        String meetImg,
        NotifyType type,
        NotificationPayload payload,
        String sendAt,
        boolean isRead,
        LocalDateTime planDate
) {
    public static List<NotificationResponse> of(
            ObjectMapper mapper,
            List<NotificationListInterface> notificationList,
            Map<Long, LocalDateTime> timeMap
    ) {

        return notificationList.stream()
                .map(notification ->
                        {
                            try {
                                return new NotificationResponse(
                                        notification.getNotificationId(),
                                        notification.getMeetId(),
                                        notification.getPlanId(),
                                        notification.getReviewId(),
                                        notification.getMeetName(),
                                        notification.getMeetImg(),
                                        notification.getType(),
                                        mapper.readValue(notification.getPayload(), NotificationPayload.class),
                                        notification.getSendAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                        notification.getReadAt() != null,
                                        getDate(notification.getPlanId(), notification.getReviewId(), timeMap)
                                );
                            } catch (JsonProcessingException e) {
                                return null;
                            }
                        }
                )
                .toList();
    }

    public interface NotificationListInterface {
        Long getNotificationId();

        Long getMeetId();

        Long getPlanId();

        Long getReviewId();

        String getMeetName();

        String getMeetImg();

        NotifyType getType();

        String getPayload();

        LocalDateTime getSendAt();

        LocalDateTime getReadAt();
    }

    private static LocalDateTime getDate(Long planId, Long reviewId, Map<Long, LocalDateTime> timeMap) {

        return timeMap.getOrDefault(planId, timeMap.get(reviewId));
    }
}
