package com.groupMeeting.global.enums.logging;

public enum LoggerKey {
    REQUEST_ID("requestId"),
    USER_INFO("userInfo"),

    EXECUTION_TIME("executionTime"),
    SUCCESS("success"),
    ERROR_MESSAGE("errorMessage"),
    STATUS("status"),
    STACK_TRACE("stackTrace"),
    QUERY("query");

    private final String key;

    LoggerKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
