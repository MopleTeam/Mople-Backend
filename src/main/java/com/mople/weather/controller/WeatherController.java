package com.mople.weather.controller;

import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherListResponse;
import com.mople.weather.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "WEATHER", description = "날씨 API")
public class WeatherController {
    private final WeatherService weatherService;

    @Operation(
            summary = "날씨 조회 API",
            description = "위도와 경도(BigDecimal)를 JSON 포맷으로 받아 날씨 정보를 리턴합니다."
    )
    @PostMapping("/weather")
    public ResponseEntity<OpenWeatherListResponse> getWeather(@RequestBody CoordinateRequest location) {
        var result = weatherService.getWeatherListByLocation(location);
        return ResponseEntity.ok(result.join());
    }
}
