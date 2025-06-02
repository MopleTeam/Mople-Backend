package com.groupMeeting.dto.response.weather;

public record WeatherInfoScheduleResponse(
        Double temperature,
        String weatherIconImage
) {
    public WeatherInfoScheduleResponse(OpenWeatherResponse weather) {
        this(weather.main().temp(), getWeatherImage(weather.weather().get(0).icon()));
    }

    public static String getWeatherImage(String icon) {
        return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
    }
}
