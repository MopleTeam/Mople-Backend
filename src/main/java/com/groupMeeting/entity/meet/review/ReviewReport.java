package com.groupMeeting.entity.meet.review;

import com.groupMeeting.entity.user.User;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "reason")
    private String reason;

    @Column(name = "review_id", updatable = false)
    private Long reviewId;

    @Column(name = "reporter_id", updatable = false)
    private Long reporterId;

    @Builder
    public ReviewReport(String reason, Long reviewId, Long reporterId) {
        this.reason = reason;
        this.reviewId = reviewId;
        this.reporterId = reporterId;
    }
}
