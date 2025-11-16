package com.mople.entity.meet.plan;

import com.mople.entity.common.BaseTimeEntity;

import com.mople.global.enums.UserRole;
import jakarta.persistence.*;

import lombok.*;

import static com.mople.global.utils.cursor.custom.sort.MemberSortExpressions.calculateNicknameTypeOrder;
import static com.mople.global.utils.cursor.custom.sort.MemberSortExpressions.calculateRoleOrder;

@Entity
@Table(name = "plan_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "participant_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "role_order", nullable = false)
    private Integer roleOrder = UserRole.PARTICIPANT.getOrder();

    @Column(name = "nickname_type_order")
    private Integer nicknameTypeOrder;

    @Column(name = "nickname_lower")
    private String nicknameLower;

    @Builder
    public PlanParticipant(Long planId, Long userId, Long reviewId, String nickName, Long hostId, Long creatorId) {
        this.planId = planId;
        this.userId = userId;
        this.reviewId = reviewId;
        this.roleOrder = calculateRoleOrder(userId, hostId, creatorId);
        this.nicknameTypeOrder = calculateNicknameTypeOrder(nickName);
        this.nicknameLower = nickName.toLowerCase();
    }
}
