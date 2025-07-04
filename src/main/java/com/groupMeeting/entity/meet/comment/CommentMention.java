package com.groupMeeting.entity.meet.comment;

import com.groupMeeting.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_mention")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private PlanComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User mentionedUser;

    @Builder
    public CommentMention(PlanComment comment, User mentionedUser) {
        this.comment = comment;
        this.mentionedUser = mentionedUser;
    }

    public void updateComment(PlanComment comment) {
        this.comment = comment;
    }
}
