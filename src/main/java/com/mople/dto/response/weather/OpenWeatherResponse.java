package com.mople.dto.response.weather;

import java.util.List;

public record OpenWeatherResponse(
        String cod,
        WeatherMain main,
        List<WeatherStatus> weather,
        WeatherCloud clouds,
        WeatherWind wind
) {
    public record WeatherMain(
            double temp,
            double feelsLike,
            int humidity
    ) {
    }

    public record WeatherStatus(
            int id,
            String main,
            String description,
            String icon
    ) {
    }

    public record WeatherCloud(
            int all
    ) {
    }

    public record WeatherWind(
            double speed,
            int deg,
            double gust
    ) {
    }
}