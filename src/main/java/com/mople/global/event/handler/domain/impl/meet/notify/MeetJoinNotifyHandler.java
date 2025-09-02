package com.mople.global.event.handler.domain.impl.meet.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.meet.MeetJoinedEvent;
import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetJoinNotifyHandler implements DomainEventHandler<MeetJoinedEvent> {

    private final MeetRepository meetRepository;
    private final UserRepository userRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<MeetJoinedEvent> supports() {
        return MeetJoinedEvent.class;
    }

    @Override
    public void handle(MeetJoinedEvent event) {
        Meet meet = meetRepository.findById(event.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        User user = userRepository.findById(event.getNewMemberId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        MeetJoinNotifyEvent notifyEvent = MeetJoinNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(meet.getName())
                .newMemberId(event.getNewMemberId())
                .newMemberNickname(user.getNickname())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
