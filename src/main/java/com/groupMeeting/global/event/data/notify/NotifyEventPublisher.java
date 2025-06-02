package com.groupMeeting.global.event.data.notify;

import com.groupMeeting.global.enums.NotifyType;

import java.util.Map;

import static com.groupMeeting.global.enums.NotifyType.*;

public record NotifyEventPublisher(
        NotifyType type,
        Map<String, String> data,
        Map<String, String> body
) {
    public static NotifyEventPublisher meetNewMember(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(MEET_NEW_MEMBER, data, body);
    }

    public static NotifyEventPublisher planNew(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(PLAN_CREATE, data, body);
    }

    public static NotifyEventPublisher planUpdate(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(PLAN_UPDATE, data, body);
    }

    public static NotifyEventPublisher planRemove(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(PLAN_DELETE, data, body);
    }

    public static NotifyEventPublisher planRemind(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(PLAN_REMIND, data, body);
    }

    public static NotifyEventPublisher reviewRemind(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(REVIEW_REMIND, data, body);
    }

    public static NotifyEventPublisher reviewUpdate(Map<String, String> data, Map<String, String> body) {
        return new NotifyEventPublisher(REVIEW_UPDATE, data, body);
    }
}
