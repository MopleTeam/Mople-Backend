package com.mople.entity.meet.plan;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.global.enums.Status;
import com.mople.dto.request.meet.plan.PlanUpdateRequest;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meet_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "plan_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "plan_time", nullable = false)
    private LocalDateTime planTime;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "lat", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "lot", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "weather_icon")
    private String weatherIcon;

    @Column(name = "weather_address", length = 50)
    private String weatherAddress;

    @Column(name = "temperature", length = 20)
    private Double temperature;

    @Column(name = "pop", length = 10)
    private Double pop;

    @Column(name = "weather_update_at")
    private LocalDateTime weatherUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    private Status status;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "meet_id", nullable = false)
    private Long meetId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public MeetPlan(String name, LocalDateTime planTime, String address, String title, BigDecimal latitude, BigDecimal longitude, String weatherAddress, Long creatorId, Long meetId) {
        this.name = name;
        this.planTime = planTime;
        this.address = address;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weatherAddress = weatherAddress;
        this.creatorId = creatorId;
        this.meetId = meetId;
        this.status = Status.ACTIVE;
    }

    public boolean isCreator(Long userId) {
        return !creatorId.equals(userId);
    }

    public boolean updatePlan(PlanUpdateRequest request) {
        boolean flag = latitude.equals(request.lat()) && longitude.equals(request.lot());

        name = request.name();
        planTime = request.planTime();
        address = request.planAddress();
        title = request.title();
        latitude = request.lat();
        longitude = request.lot();
        weatherAddress = request.weatherAddress();

        return !flag;
    }

    public boolean equalTime(LocalDateTime time) {
        return planTime.equals(time);
    }
}
