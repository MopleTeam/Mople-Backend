package com.groupMeeting.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminActiveResponse {
    private String home;
    private String plan;
    private String review;
    private String comment;
    private String user;

    public static AdminActiveResponse home() {
        return new AdminActiveResponse(
                "nav-link rounded-5 active",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5"
        );
    }

    public static AdminActiveResponse plan() {
        return new AdminActiveResponse(
                "nav-link rounded-5",
                "nav-link rounded-5 active",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5"
        );
    }

    public static AdminActiveResponse review() {
        return new AdminActiveResponse(
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5 active",
                "nav-link rounded-5",
                "nav-link rounded-5"
        );
    }

    public static AdminActiveResponse comment() {
        return new AdminActiveResponse(
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5 active",
                "nav-link rounded-5"
        );
    }

    public static AdminActiveResponse user() {
        return new AdminActiveResponse(
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5",
                "nav-link rounded-5 active"
        );
    }
}