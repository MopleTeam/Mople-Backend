package com.groupMeeting.dto.response.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenWeatherListResponse(
        String cod,
        int cnt,
        List<WeatherDetail> list
) {
    public record WeatherDetail(
            Long dt,
            WeatherMain main,
            List<WeatherStatus> weather,
            WeatherCloud clouds,
            WeatherWind wid,
            int visibility,
            Double pop,
            @JsonProperty("dt_txt")
            String dtTxt
    ) {
    }

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