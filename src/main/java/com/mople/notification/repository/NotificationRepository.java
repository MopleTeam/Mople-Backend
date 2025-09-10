package com.mople.notification.repository;

import com.mople.dto.response.notification.NotificationResponse;
import com.mople.entity.notification.Notification;

import com.mople.global.enums.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value =
            "select n.notification_id as notificationId," +
                    "n.meet_id as meetId, " +
                    "n.plan_id as planId, " +
                    "n.review_id as reviewId, " +
                    "m.name as meetName, " +
                    "m.meet_image as meetImg, " +
                    "n.type as type, " +
                    "n.payload as payload, " +
                    "n.send_at as sendAt, " +
                    "n.read_at as readAt " +
            "  from notification n " +
            "  join meet m " +
            "    on m.meet_id = n.meet_id " +
            " where n.user_id = :userId " +
            "   and n.action = :action " +
            "   and n.expired_at > now() " +
            " order by n.notification_id DESC " +
            "limit :limit",
            nativeQuery = true
    )
    List<NotificationResponse.NotificationListInterface> findNotificationFirstPage(Long userId, Action action, int limit);

    @Query(value =
            "select n.notification_id as notificationId," +
                    "n.meet_id as meetId, " +
                    "n.plan_id as planId, " +
                    "n.review_id as reviewId, " +
                    "m.name as meetName, " +
                    "m.meet_image as meetImg, " +
                    "n.type as type, " +
                    "n.payload as payload, " +
                    "n.send_at as sendAt, " +
                    "n.read_at as readAt " +
            "  from notification n " +
            "  join meet m " +
            "    on m.meet_id = n.meet_id " +
            " where n.user_id = :userId " +
            "   and n.action = :action " +
            "   and n.expired_at > now() " +
            "   and n.notification_id < :cursorId " +
            " order by n.notification_id DESC " +
            "limit :limit",
            nativeQuery = true
    )
    List<NotificationResponse.NotificationListInterface> findNotificationNextPage(Long userId, Action action, Long cursorId, int limit);

    @Query(value = "select n from Notification n where n.userId = :userId")
    List<Notification> getUserNotificationList(Long userId);

    @Query(value =
        "select count(*) " +
        "  from notification n " +
        " where n.user_id = :userId " +
        "   and n.action = :action " +
        "   and n.expired_at > now() " +
        "   and n.read_at is null ",
            nativeQuery = true
    )
    Long countBadgeCount(Long userId, Action action);

    @Query(value =
            "select 1 " +
            "  from notification " +
            " where notification_id = :cursorId " +
            " limit 1 ",
            nativeQuery = true
    )
    Optional<Integer> isCursorInvalid(Long cursorId);

    @Modifying(flushAutomatically = true)
    @Query("delete from Notification n where n.userId = :userId")
    void deleteByUserId(Long userId);

    @Query(
            "select n " +
            "  from Notification n " +
            " where n.id in :ids " +
            "   and n.action = com.mople.global.enums.Action.PENDING"
    )
    List<Notification> findAll(List<Long> ids);
}
