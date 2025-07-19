package com.mople.global.client;

import com.mople.dto.response.holiday.HolidayResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(name = "${client.holiday.name}", url = "${client.holiday.url}")
public interface HolidayClient {

    @GetMapping
    HolidayResponse getHolidayKakao(
            @RequestHeader("Authorization") String requestKey,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to
    );
}
