package com.groupMeeting.core.interceptor;

import feign.RequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClientInterceptor {
    private final String NAVER_NAME;
    private final String NAVER_ID;
    private final String NAVER_SECRET;

    private final String KAKAO_NAME;
    private final String KAKAO_SECRET;

    public ClientInterceptor(
            @Value("${client.naver.name}") String naverName,
            @Value("${naver.id}") String naverId,
            @Value("${naver.secret}") String naverSecret,
            @Value("${client.daum.name}") String kakaoName,
            @Value("${kakao.secret}") String kakaoSecret
    ) {
        this.NAVER_NAME = naverName;
        this.NAVER_ID = naverId;
        this.NAVER_SECRET = naverSecret;

        this.KAKAO_NAME = kakaoName;
        this.KAKAO_SECRET = kakaoSecret;
    }

    @Bean
    RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (requestTemplate.feignTarget().name().equals(KAKAO_NAME)) {
                requestTemplate.header("Authorization", "KakaoAK " + KAKAO_SECRET);
                requestTemplate.query("size", "15");
                requestTemplate.query("radius", "10000");
            }

            if (requestTemplate.feignTarget().name().equals(NAVER_NAME)) {
                requestTemplate.header("X-Naver-Client-Id", NAVER_ID);
                requestTemplate.header("X-Naver-Client-Secret", NAVER_SECRET);
                requestTemplate.query("display", "5");
                requestTemplate.query("sort", "random");
            }
        };
    }
}
