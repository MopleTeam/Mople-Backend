package com.groupMeeting.dto.request.user;

import com.groupMeeting.global.enums.DeviceType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserDeviceRequest(
        @NotEmpty String deviceToken,
        @NotNull DeviceType deviceType
) {}