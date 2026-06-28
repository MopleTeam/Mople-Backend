package com.mople.global.enums;

import lombok.Getter;

@Getter
public enum ExceptionReturnCode {

    // Auth
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
    NOT_FOUND_USER("404", "유저 정보가 없습니다."),

    // Policy
    EMPTY_OS("400", "운영체제가 존재하지 않습니다."),
    EMPTY_VERSION("400", "버전이 존재하지 않습니다."),
    UNSUPPORTED_OS("400", "유효하지 않은 운영체제입니다."),
    UNSUPPORTED_VERSION("400", "유효하지 않은 버전입니다."),
    NOT_FOUND_FORCE_UPDATE_POLICY("404", "강제 업데이트 정책이 존재하지 않습니다."),
    NOT_FOUND_API_VERSION_POLICY("404", "API 버전 정책이 존재하지 않습니다."),
    FORCE_UPDATE("426", "업데이트가 필요합니다."),

    // Meet
    NOT_CREATOR("403", "접근 권한이 없습니다."),
    NOT_HOST("403", "접근 권한이 없습니다."),
    UNAUTHORIZED_DELETE("403", "삭제 권한이 없습니다."),
    NOT_FOUND_MEET("404", "모임을 찾을 수 없습니다."),
    INVALID_INVITE_CODE("400", "유효하지 않은 초대 코드입니다."),
    CURRENT_MEMBER("400", "이미 존재하는 멤버입니다."),
    CURRENT_HOST("400", "현재 모임장입니다."),
    NOT_FOUND_MEMBER("404", "멤버를 찾을 수 없습니다."),
    NOT_FOUND_INVITE("404", "모임 초대정보를 찾을 수 없습니다."),
    NOT_MEMBER("403", "접근 권한이 없습니다."),

    // Post
    NOT_FOUND_POST("404", "게시글을 찾을 수 없습니다."),

    // Plan
    NOT_FOUND_PLAN("404", "일정을 찾을 수 없습니다."),
    CURRENT_PARTICIPANT("400", "이미 존재하는 멤버입니다."),
    NOT_PARTICIPANT("403", "일정에 참가한 유저만 접근할 수 있습니다."),

    // Review
    CURRENT_REVIEW("400", "이미 후기가 존재합니다."),
    NOT_FOUND_REVIEW("404", "후기를 찾을 수 없습니다."),

    // Comment
    NOT_FOUND_COMMENT("404", "댓글을 찾을 수 없습니다."),
    NOT_PARENT_COMMENT("400", "부모 댓글이 아닙니다."),
    NOT_FOUND_COMMENT_STATS("404", "댓글 정보를 찾을 수 없습니다."),
    NOT_FOUND_POST_COMMENT("404", "게시글 댓글 또는 답글을 찾을 수 없습니다."),
    NOT_FOUND_NOTICE_COMMENT("404", "공지 댓글을 찾을 수 없습니다."),

    // Notice
    NOT_FOUND_NOTICE("404", "공지를 찾을 수 없습니다."),

    // Cursor
    INVALID_CURSOR("400", "잘못된 커서입니다."),
    FAIL_DECODING_CURSOR("400", "커서를 디코딩할 수 없습니다."),

    // Report
    CURRENT_REPORT("400", "이미 신고가 존재합니다."),

    // Image
    NOT_IMAGE_REQUEST("400", "이미지 파일만 업로드 할 수 있습니다."),

    // Token
    NOT_FOUND_FIREBASE_TOKEN("404", "FCM 토큰을 찾을 수 없습니다."),

    // Notification
    NOT_FOUND_NOTIFY("404", "알림을 찾을 수 없습니다."),
    NOT_FOUND_NOTIFY_TYPE("400", "지원하지 않는 알림 유형입니다."),
    NOT_OWNER_OF_NOTIFICATION("403", "접근 권한이 없습니다."),

    // Request
    WRONG_PARAMETER("400", "잘못된 파라미터 입니다."),
    METHOD_NOT_ALLOWED("405", "허용되지 않은 메소드 입니다."),
    REQUEST_CONFLICT("409", "새로고침 후 다시 시도해주세요."),

    // Server Error
    INTERNAL_SERVER_ERROR("500", "내부 서버 에러 입니다."),
    EXTERNAL_SERVER_ERROR("500", "외부 서버 에러 입니다."),
    ILLEGAL_HANDLER_TYPE("500", "핸들러를 처리할 수 없습니다."),
    ILLEGAL_EVENT("500", "이벤트를 처리할 수 없습니다."),
    ILLEGAL_ENUM_TYPE("500", "ENUM 타입을 처리할 수 없습니다.");

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
