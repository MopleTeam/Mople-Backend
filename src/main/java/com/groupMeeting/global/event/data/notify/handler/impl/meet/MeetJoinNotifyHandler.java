package com.groupMeeting.global.event.data.notify.handler.impl.meet;

import com.groupMeeting.dto.event.data.meet.MeetJoinEventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandler;
import com.groupMeeting.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.groupMeeting.global.enums.Action.COMPLETE;
import static com.groupMeeting.global.enums.NotifyType.MEET_NEW_MEMBER;

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
