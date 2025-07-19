package com.mople.global.enums;

import lombok.Getter;

@Getter
public enum ExceptionReturnCode {
    // 인증, 인가 관련
    INVALID_KEY("400", "잘못된 KEY 입니다"),
    EXPIRED_JWT_TOKEN("400", "만료된 JWT 입니다."),
    EXPIRED_REFRESH_TOKEN("401", "재 인증이 필요합니다."),
    NOT_EXIST_BEARER_SUFFIX("400", "Bearer 접두사가 포함되지 않았습니다."),
    WRONG_JWT_TOKEN("400", "잘못된 JWT 입니다."),
    EMPTY_AUTH_JWT("400", "인증 정보가 비어있는 JWT 입니다."),
    EMPTY_USER("400", "비어있는 유저 정보로 JWT를 생성할 수 없습니다."),
    EMPTY_ACCESS("400", "액세스 토큰이 존재하지 않습니다."),
    EMPTY_REFRESH("400", "리프레시 토큰이 존재하지 않습니다."),
    ANOTHER_PROVIDER("400", "로그인 제공자가 다릅니다."),
    TOKEN_NOT_VALID("400", "ID TOKEN 인증에 실패하였습니다."),
    DUPLICATE_NICKNAME("403", "중복된 닉네임입니다."),
    NOT_USER("404", "유저 정보가 없습니다."),

    // 정책 관련
    EMPTY_OS("400", "운영체제가 존재하지 않습니다."),
    EMPTY_VERSION("400", "버전이 존재하지 않습니다."),
    UNSUPPORTED_OS("400", "유효하지 않은 운영체제입니다."),
    UNSUPPORTED_VERSION("400", "유효하지 않은 버전입니다."),
    NOT_FOUND_FORCE_UPDATE_POLICY("404", "강제 업데이트 정책이 존재하지 않습니다."),
    NOT_FOUND_API_VERSION_POLICY("404", "API 버전 정책이 존재하지 않습니다."),
    FORCE_UPDATE("426", "업데이트가 필요합니다."),

    // Meet
    NOT_CREATOR("401", "접근 권한이 없습니다."),
    NOT_FOUND_MEET("404", "모임을 찾을 수 없습니다."),
    INVALID_INVITE_CODE("400", "유효하지 않은 초대 코드입니다."),
    CURRENT_MEMBER("400", "이미 존재하는 멤버입니다."),
    NOT_FOUND_MEMBER("404", "모임에 가입한 유저만 접근할 수 있습니다."),
    NOT_FOUND_INVITE("404", "모임 초대정보를 찾을 수 없습니다."),
    NOT_MEMBER("401", "접근 권한이 없습니다."),

    // TIME
    NOT_FOUND_TIME("404", "일정의 시간을 찾을 수 없습니다."),

    // Post,
    NOT_FOUND_POST("404", "게시글을 찾을 수 없습니다."),

    // Plan
    NOT_FOUND_PLAN("404", "일정을 찾을 수 없습니다."),
    CURRENT_PARTICIPANT("400", "이미 존재하는 멤버입니다."),
    NOT_FOUND_PARTICIPANT("401", "일정에 참가한 유저만 접근할 수 있습니다."),

    // review
    CURRENT_REVIEW("400", "이미 후기가 존재합니다."),
    NOT_FOUND_REVIEW("404", "후기를 찾을 수 없습니다."),
    NOT_FOUND_REVIEW_IMAGE("404", "이미지를 찾을 수 없습니다."),

    // comment
    NOT_FOUND_COMMENT("404", "댓글을 찾을 수 없습니다."),
    NOT_PARENT_COMMENT("400", "부모 댓글이 아닙니다."),

    // cursor
    INVALID_CURSOR("400", "잘못된 커서입니다."),
    NOT_FOUND_CURSOR("404", "커서를 찾을 수 없습니다."),

    // report
    CURRENT_REPORT("400", "이미 신고가 존재합니다."),

    // Image
    NOT_IMAGE_REQUEST("400", "이미지 파일만 업로드 할 수 있습니다."),

    // Token
    NOT_FOUND_FIREBASE_TOKEN("404", "FCM 토큰을 찾을 수 없습니다."),

    // Notification
    NOT_FOUND_NOTIFY("404", "알림을 찾을 수 없습니다."),
    NOT_FOUND_NOTIFY_TYPE("400", "지원하지 않는 알림 유형입니다."),

    // 요청 관련
    WRONG_PARAMETER("400", "잘못된 파라미터 입니다."),
    METHOD_NOT_ALLOWED("405", "허용되지 않은 메소드 입니다."),

    // 내부 에러
    INTERNAL_SERVER_ERROR("500", "내부 서버 에러 입니다."),
    EXTERNAL_SERVER_ERROR("500", "외부 서버 에러 입니다.");

    private final String code;
    private final String message;

    ExceptionReturnCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer returnCode() {
        return Integer.parseInt(code);
    }
}
