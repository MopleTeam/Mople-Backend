package com.groupMeeting.test.unit.auth.type;

import com.groupMeeting.auth.factory.AuthProviderFactory;
import com.groupMeeting.auth.provider.impl.AppleAuthProvider;
import com.groupMeeting.auth.provider.impl.KakaoAuthProvider;

import com.groupMeeting.test.base.object.MockitoTest;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.enums.SocialProvider;
import com.groupMeeting.core.exception.custom.JwtException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthTypeFactoryTest extends MockitoTest {
    @Mock
    private AppleAuthProvider appleAuthProvider;

    @Mock
    private KakaoAuthProvider kakaoAuthProvider;

    @InjectMocks
    private AuthProviderFactory authProviderFactory;

    @Test
    @DisplayName("애플 제공자와 토큰을 받아 Provide Id 반환")
    void getProvideApple(){
        final SocialProvider provider = SocialProvider.APPLE;
        final String idToken = "idToken";
        final String providerId = "providerId";

        doReturn(providerId).when(appleAuthProvider).getProviderId(idToken);

        String result = authProviderFactory.getAuthProviderId(provider, idToken);

        assertEquals(providerId, result);
        verify(appleAuthProvider, times(1)).getProviderId(idToken);
    }

    @Test
    @DisplayName("카카오 제공자와 토큰을 받아 타입 Provide Id 반환")
    void getProvideKakao(){
        final SocialProvider provider = SocialProvider.KAKAO;
        final String idToken = "idToken";
        final String providerId = "providerId";

        doReturn(providerId).when(kakaoAuthProvider).getProviderId(idToken);

        String result = authProviderFactory.getAuthProviderId(provider, idToken);

        assertEquals(providerId, result);
        verify(kakaoAuthProvider, times(1)).getProviderId(idToken);
    }

    @Test
    @DisplayName("제공자가 null 이라면 예외 발생")
    void getAuthTypeNull(){
        final String idToken = "idToken";

        assertThrows(
                JwtException.class,
                () -> authProviderFactory.getAuthProviderId(null, idToken),
                ExceptionReturnCode.WRONG_JWT_TOKEN.getMessage()
        );
    }
}
