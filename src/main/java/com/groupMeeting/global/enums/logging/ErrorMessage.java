package com.groupMeeting.global.enums.logging;

public enum ErrorMessage {
    MESSAGE_404("요청한 리소스를 찾을 수 없습니다."),
    MESSAGE_403("접근 권한이 없습니다."),
    MESSAGE_4XX("예상치 못한 클라이언트 오류가 발생했습니다."),
    MESSAGE_5XX("예상치 못한 서버 오류가 발생했습니다."),
    MESSAGE_DEFAULT("예상치 못한 오류가 발생했습니다.");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
