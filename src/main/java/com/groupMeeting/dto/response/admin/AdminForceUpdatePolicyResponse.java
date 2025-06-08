package com.groupMeeting.dto.response.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminForceUpdatePolicyResponse {
    private String os;
    private int minVersion;
    private int currentVersion;
    private boolean forceUpdate;
    private String message;
}
