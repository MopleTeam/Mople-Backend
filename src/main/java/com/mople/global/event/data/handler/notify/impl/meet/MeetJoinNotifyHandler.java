package com.mople.global.event.data.handler.notify.impl.meet;

import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;

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
        return requestFactory.getMeetPushTokens(event.getNewMemberId(), event.getMeetId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(MeetJoinNotifyEvent event, List<User> users) {
        return users.stream()
                .map(u -> Notification.builder()
                        .type(event.notifyType())
                        .action(COMPLETE)
                        .meetId(event.getMeetId())
                        .payload(event.payload())
                        .userId(u.getId())
                        .build())
                .toList();
    }
}
