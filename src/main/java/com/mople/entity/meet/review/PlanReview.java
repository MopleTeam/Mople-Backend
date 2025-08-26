package com.mople.entity.meet.review;

import com.mople.entity.common.BaseTimeEntity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "review_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "name")
    private String name;

    @Column(name = "plan_time", nullable = false)
    private LocalDateTime planTime;

    @Column(name = "address", length = 120)
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

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "upload", nullable = false)
    private Boolean upload;

    @Column(name = "meet_id", nullable = false)
    private Long meetId;

    @Builder
    public PlanReview(Long planId, String name, BigDecimal latitude, BigDecimal longitude, LocalDateTime planTime, String address, String title, String weatherIcon, String weatherAddress, Double temperature, Double pop, Long creatorId, Long meetId) {
        this.planId = planId;
        this.name = name;
        this.planTime = planTime;
        this.address = address;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weatherIcon = weatherIcon;
        this.weatherAddress = weatherAddress;
        this.temperature = temperature;
        this.pop = pop;
        this.creatorId = creatorId;
        this.upload = false;
        this.meetId = meetId;
    }

    public void updateUpload() {
        this.upload = true;
    }
}