package com.mople.global.enums.event;

import com.mople.global.enums.PushTopic;

public enum NotifyType {
    PLAN_CREATE, PLAN_DELETE, PLAN_UPDATE, PLAN_REMIND,

    MEET_NEW_MEMBER,

    REVIEW_REMIND, REVIEW_UPDATE,

    COMMENT_REPLY, COMMENT_MENTION;

    public PushTopic getTopic() {
        return switch (this) {
            case MEET_NEW_MEMBER -> PushTopic.MEET;

            case PLAN_CREATE,
                 PLAN_UPDATE,
                 PLAN_DELETE,
                 PLAN_REMIND,
                 REVIEW_REMIND,
                 REVIEW_UPDATE -> PushTopic.PLAN;

            case COMMENT_REPLY -> PushTopic.REPLY;

            case COMMENT_MENTION -> PushTopic.MENTION;
        };
    }
}
