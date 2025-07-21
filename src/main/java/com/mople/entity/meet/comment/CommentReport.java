package com.mople.entity.meet.comment;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.entity.user.User;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "comment_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "reason")
    private String reason;

    @Column(name = "comment_id", updatable = false)
    private Long commentId;

    @Column(name = "reporter_id", updatable = false)
    private Long reporterId;

    @Builder
    public CommentReport(String reason, Long commentId, Long reporterId) {
        this.reason = reason;
        this.commentId = commentId;
        this.reporterId = reporterId;
    }
}