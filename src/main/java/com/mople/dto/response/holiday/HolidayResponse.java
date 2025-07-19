package com.mople.dto.response.holiday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HolidayResponse(
        List<HolidayInfo> events
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HolidayInfo(
            String title,
            HolidayTime time,
            boolean holiday
    ) {
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HolidayTime(
            @JsonProperty("start_at")
            String startAt
    ) {
    }
}