package com.mople.global.event.handler.domain.impl.meet.update;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.meet.HostChangedEvent;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.enums.notice.SystemNotice;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.service.notice.NoticeSystemService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_MEMBER;

@Component
@RequiredArgsConstructor
public class HostChangedNoticeCreator implements DomainEventHandler<HostChangedEvent> {

    private final NoticeSystemService noticeSystemService;
    private final UserRepository userRepository;

    @Override
    public Class<HostChangedEvent> getHandledType() {
        return HostChangedEvent.class;
    }

    @Override
    public void handle(HostChangedEvent event) {
        User oldHost = userRepository.findByIdAndStatus(event.oldHostId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(NOT_FOUND_MEMBER));

        User newHost = userRepository.findByIdAndStatus(event.newHostId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(NOT_FOUND_MEMBER));

        noticeSystemService.publishSystemNotice(
                event.meetId(),
                SystemNotice.HOST_CHANGED,
                oldHost.getNickname(),
                newHost.getNickname()
        );
    }
}
