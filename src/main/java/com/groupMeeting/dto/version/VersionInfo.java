package com.groupMeeting.dto.version;

import lombok.Builder;

@Builder
public record VersionInfo (
        String apiVersion
) {
}
