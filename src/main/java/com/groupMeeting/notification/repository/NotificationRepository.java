package com.groupMeeting.notification.repository;

import com.groupMeeting.dto.response.notification.NotificationListResponse;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.global.enums.Action;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("select n from Notification n where n.planId = :planId and n.action = :action")
    List<Notification> findPlanRemindNotification(Long planId, Action action);

    @Query(value =
            "select n.notification_id as notificationId," +
                   "n.meet_id as meetId, " +
                   "n.plan_id as planId, " +
                   "n.review_id as reviewId, " +
                   "m.name as meetName, " +
                   "m.meet_image as meetImg, " +
                   "n.type as type, " +
                   "n.payload as payload, " +
                   "n.send_at as sendAt" +
            "  from notification n " +
            "  join meet m" +
            "    on m.meet_id = n.meet_id" +
            " where n.user_id = :userId and n.action = CAST(:#{#action.name()} AS VARCHAR)" +
            " order by n.notification_id DESC" +
            " limit 50",
            nativeQuery = true
    )
    List<NotificationListResponse.NotificationListInterface> getUserNotificationListLimit(Long userId, Action action);

    @Query(value = "select n from Notification n join fetch n.user where n.user.id = :userId and n.action = :action")
    List<Notification> getUserNotificationList(Long userId, Action action);

    @Query("select n from Notification n join fetch n.user where n.user.id = :userId")
    List<Notification> findAllNotificationByUser(Long userId);
}
