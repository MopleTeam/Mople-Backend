package com.groupMeeting.search.controller;

import com.groupMeeting.dto.request.search.SearchKakaoPageRequest;
import com.groupMeeting.dto.request.search.SearchKakaoRequest;
import com.groupMeeting.dto.response.search.LocationKakaoResultResponses;
import com.groupMeeting.dto.response.search.LocationResultResponse;
import com.groupMeeting.search.service.LocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "장소 검색 API")
public class LocationController {
    private final LocationService locationService;

    @Deprecated
    @Operation(
            summary = "장소 검색 API -  네이버",
            description = "장소 키워드를 쿼리 파라미터로 받아 키워드에 대한 장소를 List로 반환합니다."
    )
    @GetMapping("/location/naver")
    public ResponseEntity<List<LocationResultResponse>> getLocation(@RequestParam("search") String keyword) {
        return ResponseEntity.ok(locationService.searchLocation(keyword));
    }

    @Operation(
            summary = "장소 검색 API - 카카오",
            description = "장소 키워드와 위치 정보를 통해 사용자 위치 기반 검색 결과를 반환합니다. 검색 결과가 없다면 키워드로 검색한 결과를 반환합니다."
    )
    @PostMapping("/kakao")
    public ResponseEntity<LocationKakaoResultResponses> getKakaoLocation(@RequestBody SearchKakaoRequest searchKakaoRequest) {
        return ResponseEntity.ok(locationService.searchKakaoLocation(searchKakaoRequest));
    }

    @Deprecated
    @Operation(
            summary = "장소 검색 페이징 API - 카카오",
            description = "카카오에서 제공하는 페이징을 사용할 수 있는 API입니다."
    )
    @PostMapping("/kakao/page")
    public ResponseEntity<LocationKakaoResultResponses> getKakaoLocation(
            @RequestBody @Valid SearchKakaoPageRequest searchKakaoPageRequest
    ) {
        return ResponseEntity.ok(locationService.searchKakaoPageLocation(searchKakaoPageRequest));
    }
}
