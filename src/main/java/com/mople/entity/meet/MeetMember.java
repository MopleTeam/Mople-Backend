package com.mople.entity.meet;

import jakarta.persistence.*;

import lombok.*;

import static com.mople.global.utils.cursor.custom.sort.MemberSortExpressions.*;

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

    @Column(name = "role_order", nullable = false)
    private Integer roleOrder = 3;

    @Column(name = "nickname_type_order")
    private Integer nicknameTypeOrder;

    @Column(name = "nickname_lower")
    private String nicknameLower;

    @Builder
    public MeetMember(Long meetId, Long userId, String nickName, Long hostId) {
        this.meetId = meetId;
        this.userId = userId;
        this.roleOrder = calculateRoleOrder(userId, hostId, null);
        this.nicknameTypeOrder = calculateNicknameTypeOrder(nickName);
        this.nicknameLower = nickName.toLowerCase();
    }
}
