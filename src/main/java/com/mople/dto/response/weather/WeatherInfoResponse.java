package com.mople.dto.response.weather;

public record WeatherInfoResponse(
        Double temperature,
        Double pop,
        String weatherIcon
) {
}
