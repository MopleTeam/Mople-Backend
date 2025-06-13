package com.groupMeeting.global.client;

import com.groupMeeting.dto.response.holiday.KakaoHolidayResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(name = "${client.holiday.name}", url = "${client.holiday.url}")
public interface HolidayClient {

    @GetMapping
    KakaoHolidayResponse getHolidayKakao(
            @RequestHeader("Authorization") String requestKey,
            @RequestParam("from") Instant from,
            @RequestParam("to") Instant to
    );
}
