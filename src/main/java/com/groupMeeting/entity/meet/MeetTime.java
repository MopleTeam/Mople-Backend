package com.groupMeeting.entity.meet;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meet_time")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetTime {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "time_id")
    private Long timeId;

    @Column(name = "meet_id")
    private Long meetId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_time")
    private LocalDateTime planTime;

    public MeetTime(Long meetId, Long planId, LocalDateTime planTime) {
        this.meetId = meetId;
        this.planId = planId;
        this.planTime = planTime;
    }

    public void updateTime(LocalDateTime updateTime) {
        this.planTime = updateTime;
    }
}