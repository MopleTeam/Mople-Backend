package com.groupMeeting.global.client;

import com.groupMeeting.auth.key.OidcPublicKeyList;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "${client.kakao.name}", url = "${client.kakao.public-key-url}")
public interface KakaoAuthClient {
    @GetMapping
    OidcPublicKeyList getPublicKeys();
}
