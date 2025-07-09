package com.groupMeeting.entity.meet.comment;

import com.groupMeeting.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private PlanComment comment;

    @Builder
    public CommentLike(User user, PlanComment comment) {
        this.comment = comment;
        this.user = user;
    }

    public void updateComment(PlanComment comment) {
        this.comment = comment;
    }
}
