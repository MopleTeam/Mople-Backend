package com.mople.global.event.data.notify.handler.impl.meet;

import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.NotifyType.MEET_NEW_MEMBER;

@Component
@RequiredArgsConstructor
public class MeetJoinNotifyHandler implements NotifyHandler<MeetJoinNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return MEET_NEW_MEMBER;
    }

    @Override
    public Class<MeetJoinNotifyEvent> getHandledType() {
        return MeetJoinNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(MeetJoinNotifyEvent data, NotificationEvent notify) {
        return requestFactory.getMeetPushTokens(data.getNewMemberId(), data.getMeetId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(MeetJoinNotifyEvent data, NotificationEvent notify, List<User> users) {
        return users.stream()
                .map(u -> Notification.builder()
                        .type(getType())
                        .action(COMPLETE)
                        .meetId(data.getMeetId())
                        .payload(notify.payload())
                        .user(u)
                        .build())
                .toList();
    }
}
