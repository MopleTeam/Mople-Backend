package com.mople.entity.notification;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.Action;
import com.mople.global.enums.NotifyType;

import io.hypersistence.utils.hibernate.type.json.JsonType;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "notification_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 15)
    private NotifyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 30)
    private Action action;

    @Column(name = "meet_id", length = 30)
    private Long meetId;

    @Column(name = "plan_id", length = 30)
    private Long planId;

    @Column(name = "review_id", length = 30)
    private Long reviewId;

    @Column(name = "user_id")
    private Long userId;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private NotificationPayload payload;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder
    public Notification(Action action, Long meetId, Long planId, Long reviewId, Long userId, NotificationPayload payload, NotifyType type, LocalDateTime scheduledAt) {
        this.type = type;
        this.action = action;
        this.meetId = meetId;
        this.planId = planId;
        this.reviewId = reviewId;
        this.userId = userId;
        this.payload = payload;
        this.sendAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusDays(30);
        this.scheduledAt = scheduledAt;
    }

    public void updateNotification(NotifyEvent event){
        this.type = event.notifyType();
        this.payload = event.payload();
        this.sendAt = LocalDateTime.now();
        this.action = Action.COMPLETE;
    }

    public void updateReadAt() {
        this.readAt = LocalDateTime.now();
    }
}
