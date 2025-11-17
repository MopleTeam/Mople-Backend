package com.mople.global.enums.notice;

public enum SystemNotice {
    HOST_CHANGED("모임장이 %s님에서 %s님으로 변경되었습니다."),
    MEET_JOINED("%s님이 모임에 참여하였습니다.");

    private final String content;

    SystemNotice(String content) {
        this.content = content;
    }

    public String format(Object... args) {
        return String.format(content, args);
    }
}