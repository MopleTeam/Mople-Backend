package com.mople.auth.provider.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.auth.key.OidcPublicKeyList;
import com.mople.auth.provider.OidcProvider;
import com.mople.auth.provider.PublicKeyProvider;
import com.mople.global.client.KakaoAuthClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
@RequiredArgsConstructor
public class KakaoAuthProvider implements OidcProvider {
    private final KakaoAuthClient kakaoClient;
    private final JwtProvider jwtProvider;
    private final PublicKeyProvider publicKeyProvider;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderId(final String idToken) {
        final OidcPublicKeyList oidcPublicKeyList = kakaoClient.getPublicKeys();
        final PublicKey publicKey = publicKeyProvider.generatePublicKey(parseHeaders(idToken, objectMapper), oidcPublicKeyList);

        return jwtProvider.parseClaims(idToken, publicKey).getSubject();
    }
}
