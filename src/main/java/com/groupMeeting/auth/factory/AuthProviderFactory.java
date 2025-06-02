package com.groupMeeting.auth.factory;

import com.groupMeeting.auth.provider.impl.AppleAuthProvider;
import com.groupMeeting.auth.provider.impl.KakaoAuthProvider;
import com.groupMeeting.auth.provider.OidcProvider;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.enums.SocialProvider;
import com.groupMeeting.core.exception.custom.JwtException;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class AuthProviderFactory {
    private final Map<SocialProvider, OidcProvider> authTypeMap;
    private final AppleAuthProvider appleAuthProvider;
    private final KakaoAuthProvider kakaoAuthProvider;

    public AuthProviderFactory(
            AppleAuthProvider appleAuthProvider,
            KakaoAuthProvider kakaoAuthProvider
    ) {
        this.authTypeMap = new EnumMap<>(SocialProvider.class);
        this.appleAuthProvider = appleAuthProvider;
        this.kakaoAuthProvider = kakaoAuthProvider;

        init();
    }

    private void init() {
        authTypeMap.put(SocialProvider.APPLE, appleAuthProvider);
        authTypeMap.put(SocialProvider.KAKAO, kakaoAuthProvider);
    }

    public String getAuthProviderId(SocialProvider provider, String idToken) {
        return getProvider(provider).getProviderId(idToken);
    }

    private OidcProvider getProvider(SocialProvider provider) {
        OidcProvider oidcProvider = authTypeMap.get(provider);

        if (oidcProvider == null) {
            throw new JwtException(ExceptionReturnCode.EXTERNAL_SERVER_ERROR);
        }

        return oidcProvider;
    }
}
