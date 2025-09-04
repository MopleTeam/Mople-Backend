package com.mople.global.event.handler.notify.impl.meet;

import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MeetJoinNotifyHandler implements NotifyEventHandler<MeetJoinNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<MeetJoinNotifyEvent> getHandledType() {
        return MeetJoinNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(MeetJoinNotifyEvent event) {
        return requestFactory.buildForTargets(event.getTargetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(MeetJoinNotifyEvent event, List<Long> userIds) {
        return userIds.stream()
                .map(userId -> Notification.builder()
                        .type(event.notifyType())
                        .meetId(event.getMeetId())
                        .payload(event.payload())
                        .userId(userId)
                        .build())
                .toList();
    }
}
