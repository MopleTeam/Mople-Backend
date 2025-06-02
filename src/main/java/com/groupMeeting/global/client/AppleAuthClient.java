package com.groupMeeting.global.client;

import com.groupMeeting.auth.key.OidcPublicKeyList;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "${client.apple.name}",  url = "${client.apple.public-key-url}")
public interface AppleAuthClient {
    @GetMapping
    OidcPublicKeyList getPublicKeys();
}
