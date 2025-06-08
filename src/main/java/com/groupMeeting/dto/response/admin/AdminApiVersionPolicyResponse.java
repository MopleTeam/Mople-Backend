package com.groupMeeting.dto.response.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminApiVersionPolicyResponse {
    private String os;
    private String uri;
    private int appVersion;
    private String apiVersion;
    private String description;
}
