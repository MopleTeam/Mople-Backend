package com.groupMeeting.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminHeaderResponse {
    private String home;
    private String plan;
    private String review;
    private String comment;
    private String user;

    public static AdminHeaderResponse home() {
        return new AdminHeaderResponse(
                "true",
                "false",
                "false",
                "false",
                "false"
        );
    }

    public static AdminHeaderResponse plan() {
        return new AdminHeaderResponse(
                "false",
                "true",
                "false",
                "false",
                "false"
        );
    }

    public static AdminHeaderResponse review() {
        return new AdminHeaderResponse(
                "false",
                "false",
                "true",
                "false",
                "false"
        );
    }

    public static AdminHeaderResponse comment() {
        return new AdminHeaderResponse(
                "false",
                "false",
                "false",
                "true",
                "false"
        );
    }

    public static AdminHeaderResponse user() {
        return new AdminHeaderResponse(
                "false",
                "false",
                "false",
                "false",
                "true"
        );
    }
}