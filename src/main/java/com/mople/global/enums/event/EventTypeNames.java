package com.mople.global.enums.event;

public final class EventTypeNames {

    // MEET
    public static final String MEET_SOFT_DELETED = "MEET_SOFT_DELETED";
    public static final String MEET_IMAGE_CHANGED = "MEET_IMAGE_CHANGED";
    public static final String MEET_JOINED = "MEET_JOINED";
    public static final String MEET_LEFT = "MEET_LEFT";
    public static final String MEET_HOST_CHANGED = "MEET_HOST_CHANGED";
    public static final String MEET_PURGE = "MEET_PURGE";

    // NOTICE
    public static final String NOTICE_SOFT_DELETED = "NOTICE_SOFT_DELETED";
    public static final String NOTICE_PURGE = "NOTICE_PURGE";

    // PLAN
    public static final String PLAN_CREATED = "PLAN_CREATED";
    public static final String PLAN_SOFT_DELETED = "PLAN_SOFT_DELETED";  // 작성자로 탈퇴 시 작성자 PLAN 모두 삭제
    public static final String PLAN_TIME_CHANGED = "PLAN_TIME_CHANGED";
    public static final String PLAN_TRANSITION_REQUESTED = "PLAN_TRANSITION_REQUESTED";
    public static final String PLAN_TRANSITIONED = "PLAN_TRANSITIONED";
    public static final String PLAN_REMIND = "PLAN_REMIND";  // 대상이 참여자 전부, 참여자로 탈퇴 시 참여자 삭제되므로 이벤트 cancel X
    public static final String PLAN_NO_LOCATION = "PLAN_NO_LOCATION";
    public static final String PLAN_PURGE = "PLAN_PURGE";

    // REVIEW
    public static final String REVIEW_SOFT_DELETED = "REVIEW_SOFT_DELETED";  // 작성자로 탈퇴 시 작성자 REVIEW 남아 있음
    public static final String REVIEW_UPLOAD = "REVIEW_UPLOAD";
    public static final String REVIEW_IMAGE_REMOVE = "REVIEW_IMAGE_REMOVE";
    public static final String REVIEW_REMIND = "REVIEW_REMIND";  // 대상이 작성자 고정, 작성자로 탈퇴 시 이벤트 cancel 처리
    public static final String REVIEW_PURGE = "REVIEW_PURGE";

    // COMMENT
    public static final String COMMENT_CREATED = "COMMENT_CREATED";
    public static final String COMMENTS_SOFT_DELETED = "COMMENTS_SOFT_DELETED";
    public static final String COMMENT_MENTION_ADDED = "COMMENT_MENTION_ADDED";
    public static final String COMMENTS_PURGE = "COMMENTS_PURGE";

    // IMAGE
    public static final String IMAGE_DELETED = "IMAGE_DELETED";

    // USER
    public static final String USER_IMAGE_CHANGED = "USER_IMAGE_CHANGED";
    public static final String USER_NICKNAME_CHANGED = "USER_NICKNAME_CHANGED";
    public static final String USER_DELETED = "USER_DELETED";

    // NOTIFY
    public static final String NOTIFY_REQUESTED = "NOTIFY_REQUESTED";

    // WEATHER
    public static final String WEATHER_REFRESH_REQUESTED = "WEATHER_REFRESH_REQUESTED";
}
