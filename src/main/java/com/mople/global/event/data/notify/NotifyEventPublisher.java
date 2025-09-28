package com.mople.global.event.data.notify;

import com.mople.dto.event.data.EventData;
import com.mople.global.enums.NotifyType;

import static com.mople.global.enums.NotifyType.*;

public record NotifyEventPublisher(
        NotifyType type,
        EventData data
) {
    public static NotifyEventPublisher meetNewMember(EventData data) {
        return new NotifyEventPublisher(MEET_NEW_MEMBER, data);
    }

    public static NotifyEventPublisher planNew(EventData data) {
        return new NotifyEventPublisher(PLAN_CREATE, data);
    }

    public static NotifyEventPublisher planUpdate(EventData data) {
        return new NotifyEventPublisher(PLAN_UPDATE, data);
    }

    public static NotifyEventPublisher planRemove(EventData data) {
        return new NotifyEventPublisher(PLAN_DELETE, data);
    }

    public static NotifyEventPublisher planRemind(EventData data) {
        return new NotifyEventPublisher(PLAN_REMIND, data);
    }

    public static NotifyEventPublisher reviewRemind(EventData data) {
        return new NotifyEventPublisher(REVIEW_REMIND, data);
    }

    public static NotifyEventPublisher reviewUpdate(EventData data) {
        return new NotifyEventPublisher(REVIEW_UPDATE, data);
    }

    public static NotifyEventPublisher commentReply(EventData data) {
        return new NotifyEventPublisher(COMMENT_REPLY, data);
    }

    public static NotifyEventPublisher commentMention(EventData data) {
        return new NotifyEventPublisher(COMMENT_MENTION, data);
    }
}
