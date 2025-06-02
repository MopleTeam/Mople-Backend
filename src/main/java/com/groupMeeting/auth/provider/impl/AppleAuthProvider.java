package com.groupMeeting.auth.provider.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.groupMeeting.auth.key.OidcPublicKeyList;
import com.groupMeeting.auth.provider.OidcProvider;
import com.groupMeeting.auth.provider.PublicKeyProvider;
import com.groupMeeting.global.client.AppleAuthClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
@RequiredArgsConstructor
public class AppleAuthProvider implements OidcProvider {
    private final AppleAuthClient appleClient;
    private final JwtProvider jwtProvider;
    private final PublicKeyProvider publicKeyProvider;
    private final ObjectMapper objectMapper;

    @Override
    public String getProviderId(final String idToken) {
        final OidcPublicKeyList oidcPublicKeyList = appleClient.getPublicKeys();
        final PublicKey publicKey = publicKeyProvider.generatePublicKey(parseHeaders(idToken, objectMapper), oidcPublicKeyList);

        return jwtProvider.parseClaims(idToken, publicKey).getSubject();
    }
}
