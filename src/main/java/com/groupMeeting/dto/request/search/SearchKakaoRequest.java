package com.groupMeeting.dto.request.search;

import lombok.Getter;

public record SearchKakaoRequest(
        @Getter String query,
        @Getter String x,
        @Getter String y
) {
}
