package com.groupMeeting.global.client;

import com.groupMeeting.dto.response.search.LocationSearchResponses;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${client.naver.name}", url = "${client.naver.url}")
public interface NaverLocationClient {
    @GetMapping
    LocationSearchResponses getLocation(@RequestParam String query);
}