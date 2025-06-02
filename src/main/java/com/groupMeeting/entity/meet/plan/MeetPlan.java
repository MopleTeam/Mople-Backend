package com.groupMeeting.entity.meet.plan;

import com.groupMeeting.dto.response.weather.WeatherInfoResponse;
import com.groupMeeting.entity.common.BaseTimeEntity;
import com.groupMeeting.entity.meet.Meet;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.dto.request.meet.plan.PlanUpdateRequest;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "meet_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetPlan extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "plan_id")
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id")
    private Meet meet;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanParticipant> participants = new ArrayList<>();

    @Builder
    public MeetPlan(String name, LocalDateTime planTime, String address, String title, BigDecimal latitude, BigDecimal longitude, String weatherAddress, User creator, Meet meet, Status status) {
        this.name = name;
        this.planTime = planTime;
        this.address = address;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weatherAddress = weatherAddress;
        this.creator = creator;
        this.meet = meet;
        this.status = status;
    }

    public void updateWeather(WeatherInfoResponse response) {
        if (response != null) {
            this.weatherIcon = response.weatherIcon();
            this.temperature = response.temperature();
            this.pop = response.pop();
            this.weatherUpdatedAt = LocalDateTime.now();
        }
    }

    public void deleteWeatherInfo() {
        this.weatherIcon = null;
        this.temperature = null;
        this.pop = null;
        this.weatherUpdatedAt = LocalDateTime.now();
    }

    public void updateMeet(Meet meet) {
        this.meet = meet;
    }

    public void removeMeet() {
        this.meet = null;
    }

    public void addParticipant(PlanParticipant participant) {
        participant.updatePlan(this);
        participants.add(participant);
    }

    public void removeParticipant(Long userId) {
        participants.removeIf(participant -> !creator.getId().equals(userId) && participant.getUser().getId().equals(userId));
    }

    public boolean isCreator(Long userId) {
        return !creator.getId().equals(userId);
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

    public boolean findParticipantInUser(Long userId) {
        return participants
                .stream()
                .noneMatch(participant -> participant.getUser().getId().equals(userId));
    }

    public Optional<PlanParticipant> getParticipantById(Long userId) {
        return participants
                .stream()
                .filter(participant -> participant.getUser().getId().equals(userId))
                .findFirst();
    }

    public boolean equalTime(LocalDateTime time) {
        return planTime.equals(time);
    }
}
