package com.mople.global.event.handler.notify.impl.comment;

import com.mople.dto.event.data.notify.comment.CommentMentionNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentMentionNotifyHandler implements NotifyEventHandler<CommentMentionNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<CommentMentionNotifyEvent> getHandledType() {
        return CommentMentionNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(CommentMentionNotifyEvent event) {
        return requestFactory.buildForTargets(event.targetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(CommentMentionNotifyEvent event, List<Long> userIds) {
        if (event.planId() != null && event.reviewId() == null) {
            return userIds.stream()
                    .map(userId ->
                            Notification.builder()
                                    .type(event.notifyType())
                                    .meetId(event.meetId())
                                    .planId(event.planId())
                                    .payload(event.payload())
                                    .userId(userId)
                                    .build()
                    )
                    .toList();
        }

        return userIds.stream()
                .map(userId ->
                        Notification.builder()
                                .type(event.notifyType())
                                .meetId(event.meetId())
                                .reviewId(event.reviewId())
                                .payload(event.payload())
                                .userId(userId)
                                .build()
                )
                .toList();
    }
}
