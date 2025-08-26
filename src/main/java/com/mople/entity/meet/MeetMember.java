package com.mople.entity.meet;

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

    @Column(name = "meet_id", nullable = false)
    private Long meetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public MeetMember(Long meetId, Long userId) {
        this.meetId = meetId;
        this.userId = userId;
    }
}
