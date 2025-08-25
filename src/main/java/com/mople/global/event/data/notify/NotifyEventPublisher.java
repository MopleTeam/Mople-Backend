package com.mople.global.event.data.notify;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.global.enums.NotifyType;

import static com.mople.global.enums.NotifyType.*;

public record NotifyEventPublisher(
        NotifyType type,
        NotifyEvent data
) {
    public static NotifyEventPublisher meetNewMember(NotifyEvent data) {
        return new NotifyEventPublisher(MEET_NEW_MEMBER, data);
    }

    public static NotifyEventPublisher planNew(NotifyEvent data) {
        return new NotifyEventPublisher(PLAN_CREATE, data);
    }

    public static NotifyEventPublisher planUpdate(NotifyEvent data) {
        return new NotifyEventPublisher(PLAN_UPDATE, data);
    }

    public static NotifyEventPublisher planRemove(NotifyEvent data) {
        return new NotifyEventPublisher(PLAN_DELETE, data);
    }

    public static NotifyEventPublisher planRemind(NotifyEvent data) {
        return new NotifyEventPublisher(PLAN_REMIND, data);
    }

    public static NotifyEventPublisher reviewRemind(NotifyEvent data) {
        return new NotifyEventPublisher(REVIEW_REMIND, data);
    }

    public static NotifyEventPublisher reviewUpdate(NotifyEvent data) {
        return new NotifyEventPublisher(REVIEW_UPDATE, data);
    }

    public static NotifyEventPublisher commentReply(NotifyEvent data) {
        return new NotifyEventPublisher(COMMENT_REPLY, data);
    }

    public static NotifyEventPublisher commentMention(NotifyEvent data) {
        return new NotifyEventPublisher(COMMENT_MENTION, data);
    }
}
