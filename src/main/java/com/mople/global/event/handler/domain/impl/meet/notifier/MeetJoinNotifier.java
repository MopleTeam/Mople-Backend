package com.mople.global.event.handler.domain.impl.meet.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.meet.MeetJoinedEvent;
import com.mople.dto.event.data.domain.global.NotifyRequestedEvent;
import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.repository.NotificationRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class MeetJoinNotifier implements DomainEventHandler<MeetJoinedEvent> {

    private final MeetRepository meetRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<MeetJoinedEvent> getHandledType() {
        return MeetJoinedEvent.class;
    }

    @Override
    public void handle(MeetJoinedEvent event) {
        List<Long> targetIds = userReader.findMeetUsersNoTriggers(event.newMemberId(), event.meetId());

        if (targetIds.isEmpty()) {
            return;
        }

        Meet meet = meetRepository.findByIdAndStatus(event.meetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        User user = userRepository.findByIdAndStatus(event.newMemberId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        MeetJoinNotifyEvent notifyEvent = MeetJoinNotifyEvent.builder()
                .meetId(event.meetId())
                .meetName(meet.getName())
                .newMemberNickname(user.getNickname())
                .build();

        List<Long> notificationIds = notificationRepository.saveAll(
                        targetIds.stream()
                                .map(targetId ->
                                        Notification.builder()
                                                .type(notifyEvent.notifyType())
                                                .meetId(meet.getId())
                                                .payload(notifyEvent.payload())
                                                .userId(targetId)
                                                .build()
                                )
                                .toList()
                ).stream()
                .map(Notification::getId).toList();

        outboxService.save(
                NOTIFY_REQUESTED,
                MEET,
                meet.getId(),
                new NotifyRequestedEvent(notifyEvent, notificationIds)
        );
    }
}
