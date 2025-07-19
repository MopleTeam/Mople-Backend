package com.mople.entity.meet.plan;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanReport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "reason")
    private String reason;

    @Column(name = "plan_id", updatable = false)
    private Long planId;

    @Column(name = "reporter_id", updatable = false)
    private Long reporterId;

    @Builder
    public PlanReport(String reason, Long planId, Long reporterId) {
        this.reason = reason;
        this.planId = planId;
        this.reporterId = reporterId;
    }
}
