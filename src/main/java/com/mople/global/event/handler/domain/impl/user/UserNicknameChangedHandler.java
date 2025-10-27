package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.user.UserNicknameChangedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.global.utils.cursor.custom.sort.MemberSortExpressions;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNicknameChangedHandler implements DomainEventHandler<UserNicknameChangedEvent> {

    private final MeetMemberRepository memberRepository;
    private final PlanParticipantRepository participantRepository;

    @Override
    public Class<UserNicknameChangedEvent> getHandledType() {
        return UserNicknameChangedEvent.class;
    }

    @Override
    public void handle(UserNicknameChangedEvent event) {
        String lower = event.newNickname().toLowerCase();
        Integer typeOrder = MemberSortExpressions.calculateNicknameTypeOrder(event.newNickname());

        memberRepository.updateNickname(event.userId(), lower, typeOrder);
        participantRepository.updateNickname(event.userId(), lower, typeOrder);
    }
}
