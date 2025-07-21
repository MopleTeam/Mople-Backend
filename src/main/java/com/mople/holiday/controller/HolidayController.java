package com.mople.holiday.controller;

import com.mople.dto.client.HolidayClientResponse;
import com.mople.holiday.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/holiday")
@RequiredArgsConstructor
@Tag(name = "HOLIDAY", description = "공휴일 API")
public class HolidayController {

    private final HolidayService holidayService;

    @Operation(
            summary = "공휴일 조회 API",
            description = "공휴일을 조회합니다. 파라미터를 통해 1년 단위 기준으로 조회합니다."
    )
    @GetMapping
    public ResponseEntity<List<HolidayClientResponse>> getHoliday(@RequestParam String year) {

        return ResponseEntity.ok(holidayService.getHolidayMonth(year));
    }
}
