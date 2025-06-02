package com.groupMeeting.global.client;

import com.groupMeeting.dto.request.search.SearchKakaoPageRequest;
import com.groupMeeting.dto.request.search.SearchKakaoRequest;
import com.groupMeeting.dto.response.search.LocationKakaoSearchResponses;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${client.daum.name}", url = "${client.daum.url}")
public interface KakaoLocationClient {
    @GetMapping
    LocationKakaoSearchResponses getLocation(@RequestParam String query);

    @GetMapping
    LocationKakaoSearchResponses getUserLocation(@SpringQueryMap SearchKakaoRequest request);

    @GetMapping
    LocationKakaoSearchResponses getPageLocation(@SpringQueryMap SearchKakaoPageRequest request);
}