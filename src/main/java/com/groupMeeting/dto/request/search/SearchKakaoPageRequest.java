package com.groupMeeting.dto.request.search;

import jakarta.validation.constraints.Min;

import lombok.Getter;

@Getter
public class SearchKakaoPageRequest {
    private String query;
    private String x;
    private String y;
    @Min(value = 1) private int page;
}
