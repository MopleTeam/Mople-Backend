package com.groupMeeting.dto.response.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupMeeting.global.enums.NotifyType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public record NotificationListResponse(
        Long notificationId,
        Long meetId,
        Long planId,
        Long reviewId,
        String meetName,
        String meetImg,
        NotifyType type,
        NotificationPayload payload,
        String sendAt,
        LocalDate planDate
) {
    public static List<NotificationListResponse> of(ObjectMapper mapper, List<NotificationListInterface> notificationList, Map<Long, LocalDateTime> planTimeMap) {

        return notificationList.stream()
                .map(notification ->
                        {
                            try {
                                return new NotificationListResponse(
                                        notification.getNotificationId(),
                                        notification.getMeetId(),
                                        notification.getPlanId(),
                                        notification.getReviewId(),
                                        notification.getMeetName(),
                                        notification.getMeetImg(),
                                        notification.getType(),
                                        mapper.readValue(notification.getPayload(), NotificationPayload.class),
                                        notification.getSendAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                        isNull(planTimeMap.get(notification.getPlanId())) ? null : planTimeMap.get(notification.getPlanId()).toLocalDate()
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
    }
}
