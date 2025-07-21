package com.mople.global.enums.logging;

import lombok.Getter;

@Getter
public enum LoggerMessage {
    // internal api 로그
    INTERNAL_API_SUCCESS("Internal API 응답 성공"),
    INTERNAL_API_SLOW_RESPONSE("Internal API 느린 응답 감지"),
    INTERNAL_API_FAIL("Internal API 응답 실패"),
    INTERNAL_API_FAIL_WITH_NO_EXCEPTION("Exception 없는 예외 발생"),

    // external api 로그
    EXTERNAL_API_SUCCESS("External API 응답 성공"),
    EXTERNAL_API_SLOW_RESPONSE("External API 느린 응답 감지"),
    EXTERNAL_API_FAIL("External API 응답 실패"),

    // business logic 로그
    BUSINESS_LOGIC_SUCCESS("Business logic 응답 성공"),
    BUSINESS_LOGIC_SLOW_RESPONSE("Business logic 느린 응답 감지"),
    BUSINESS_LOGIC_FAIL("Business logic 응답 실패"),

    // slow query 로그
    SLOW_QUERY_DETECTED("Slow query 감지"),

    // exception 로그
    EXCEPTION_CLIENT("Exception 클라이언트 오류 발생"),
    EXCEPTION_SERVER("Exception 서버 오류 발생");

    private final String message;

    LoggerMessage(String message) {
        this.message = message;
    }
}
