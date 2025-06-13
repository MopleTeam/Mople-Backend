package com.groupMeeting.global.enums.logging;

public enum LoggerMessage {
    // internal api 로그
    INTERNAL_API_SUCCESS("Internal API 응답 성공"),
    INTERNAL_API_SLOW_RESPONSE("Internal API 느린 응답 감지"),
    INTERNAL_API_FAIL("Internal API 응답 실패"),
    INTERNAL_API_FAIL_WITH_NO_EXCEPTION("Exception 없는 예외 발생");

    private final String message;

    LoggerMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
