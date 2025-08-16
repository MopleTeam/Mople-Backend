package com.mople.search.service;

import com.mople.dto.request.search.SearchKakaoPageRequest;
import com.mople.dto.request.search.SearchKakaoRequest;
import com.mople.dto.response.search.LocationKakaoResultResponses;
import com.mople.dto.response.search.LocationKakaoSearchResponses;
import com.mople.dto.response.search.LocationResultResponse;
import com.mople.global.client.KakaoLocationClient;
import com.mople.global.client.NaverLocationClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class LocationService {
    private final NaverLocationClient naverLocationClient;
    private final KakaoLocationClient kakaoLocationClient;

    public List<LocationResultResponse> searchLocation(String keyword) {
        return naverLocationClient.getLocation(keyword).items().stream().map(LocationResultResponse::new).toList();
    }

    public LocationKakaoResultResponses searchKakaoLocation(SearchKakaoRequest request) {
        LocationKakaoSearchResponses userLocation = kakaoLocationClient.getUserLocation(request);

        if(userLocation.documents().isEmpty()) {
            return new LocationKakaoResultResponses(kakaoLocationClient.getLocation(request.query()));
        } else if(userLocation.documents().size() < 10){
            userLocation.documents().addAll(kakaoLocationClient.getLocation(request.query()).documents());
        }

        return new LocationKakaoResultResponses(userLocation);
    }

    public LocationKakaoResultResponses searchKakaoPageLocation(SearchKakaoPageRequest request) {
        return new LocationKakaoResultResponses(kakaoLocationClient.getPageLocation(request));
    }
}
