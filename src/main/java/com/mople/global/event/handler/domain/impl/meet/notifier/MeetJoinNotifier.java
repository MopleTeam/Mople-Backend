package com.mople.global.event.handler.domain.impl.meet.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.meet.MeetJoinedEvent;
import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.service.NotificationSendService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MeetJoinNotifier implements DomainEventHandler<MeetJoinedEvent> {

    private final MeetRepository meetRepository;
    private final UserRepository userRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<MeetJoinedEvent> getHandledType() {
        return MeetJoinedEvent.class;
    }

    @Override
    public void handle(MeetJoinedEvent event) {
        List<Long> targetIds = userReader.findMeetUsersNoTriggers(event.getNewMemberId(), event.getMeetId());

        Meet meet = meetRepository.findByIdAndStatus(event.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        User user = userRepository.findByIdAndStatus(event.getNewMemberId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        MeetJoinNotifyEvent notifyEvent = MeetJoinNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(meet.getName())
                .newMemberNickname(user.getNickname())
                .targetIds(targetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
