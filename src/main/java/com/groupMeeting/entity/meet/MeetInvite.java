package com.groupMeeting.entity.meet;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meet_invite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "invite_id")
    private Long id;

    @Column(name = "invite_code")
    private String inviteCode;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "meet_id")
    private Long meetId;

    @Builder
    public MeetInvite(Long meetId) {
        this.inviteCode = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusDays(3);
        this.meetId = meetId;
    }

    public void generateInviteCode() {
        inviteCode = UUID.randomUUID().toString();
    }

    public String getInviteUrl(String inviteUrl) {
        return "\"https://" + inviteUrl + "/invite?code=" + inviteCode + "\"";
    }
}
