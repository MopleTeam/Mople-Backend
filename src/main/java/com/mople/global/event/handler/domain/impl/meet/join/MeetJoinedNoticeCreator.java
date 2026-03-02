package com.mople.global.event.handler.domain.impl.meet.join;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.meet.MeetJoinedEvent;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.enums.notice.SystemNotice;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.service.notice.NoticeSystemService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_MEMBER;

@Deprecated
//@Component
@RequiredArgsConstructor
public class MeetJoinedNoticeCreator implements DomainEventHandler<MeetJoinedEvent> {

    private final NoticeSystemService noticeSystemService;
    private final UserRepository userRepository;

    @Override
    public Class<MeetJoinedEvent> getHandledType() {
        return MeetJoinedEvent.class;
    }

    @Override
    public void handle(MeetJoinedEvent event) {

        User newMember = userRepository.findByIdAndStatus(event.newMemberId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(NOT_FOUND_MEMBER));

        noticeSystemService.publishSystemNotice(
                event.meetId(),
                SystemNotice.MEET_JOINED,
                newMember.getNickname()
        );
    }
}
