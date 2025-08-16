package com.mople.entity.notification;

import com.mople.dto.response.notification.NotificationPayload;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.NotifyType;

import com.mople.global.event.data.notify.NotificationEvent;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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
    public Notification(Action action, Long meetId, Long planId, Long reviewId, User user, NotificationPayload payload, NotifyType type, LocalDateTime scheduledAt) {
        this.type = type;
        this.action = action;
        this.meetId = meetId;
        this.planId = planId;
        this.reviewId = reviewId;
        this.user = user;
        this.payload = payload;
        this.sendAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusYears(99);
        this.scheduledAt = scheduledAt;
    }

    public void updateNotification(NotificationEvent notify, NotifyType type){
        this.type = type;
        this.payload = notify.payload();
        this.sendAt = LocalDateTime.now();
        this.action = Action.COMPLETE;
    }

    public void updateReadAt() {
        this.readAt = LocalDateTime.now();
    }
}
