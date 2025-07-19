package com.mople.global.event.data.notify.handler.impl.meet;

import com.mople.dto.event.data.meet.MeetJoinEventData;
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
public class MeetJoinNotifyHandler implements NotifyHandler<MeetJoinEventData> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return MEET_NEW_MEMBER;
    }

    @Override
    public Class<MeetJoinEventData> getHandledType() {
        return MeetJoinEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(MeetJoinEventData data, NotificationEvent notify) {
        return requestFactory.getMeetPushToken(data.getNewMemberId(), data.getMeetId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(MeetJoinEventData data, NotificationEvent notify, List<User> users) {
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
