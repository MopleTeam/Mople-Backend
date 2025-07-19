package com.mople.entity.meet;

import com.mople.entity.user.User;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "meet_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetMember {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "meet_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id")
    private Meet joinMeet;

    @Builder
    public MeetMember(User user, Meet joinMeet) {
        this.user = user;
        this.joinMeet = joinMeet;
    }

    public void joinMeet(Meet meet) {
        joinMeet = meet;
    }

    public boolean findAnyUser(Long userId){
        return user.getId().equals(userId);
    }
}
