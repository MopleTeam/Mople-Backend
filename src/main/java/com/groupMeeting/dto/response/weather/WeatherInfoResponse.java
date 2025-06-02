package com.groupMeeting.dto.response.weather;

public record WeatherInfoResponse(
        Double temperature,
        Double pop,
        String weatherIcon
) {
}
