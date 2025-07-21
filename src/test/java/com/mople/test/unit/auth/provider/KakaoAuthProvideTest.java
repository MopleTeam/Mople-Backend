package com.mople.test.unit.auth.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.auth.key.OidcPublicKeyList;
import com.mople.auth.provider.impl.AppleAuthProvider;
import com.mople.auth.provider.impl.JwtProvider;
import com.mople.auth.provider.impl.KakaoAuthProvider;
import com.mople.auth.provider.PublicKeyProvider;
import com.mople.test.base.object.MockitoTest;
import com.mople.global.client.KakaoAuthClient;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.core.exception.custom.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class KakaoAuthProvideTest extends MockitoTest{
    @Mock
    private KakaoAuthClient kakaoAuthClient;

    @Mock
    private PublicKeyProvider publicKeyProvider;

    @Mock
    private JwtProvider jwtProvider;

    private KakaoAuthProvider kakaoAuthProvider;

    private final String idToken = "eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.payload.signature";

    @BeforeEach
    public void setUp(){
        kakaoAuthProvider = new KakaoAuthProvider(
                kakaoAuthClient,
                jwtProvider,
                publicKeyProvider,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Token으로 제공자 Id를 받는다")
    void getProviderId(){
        OidcPublicKeyList  oidcPublicKeyList = getConstructorMonkey().giveMeOne(OidcPublicKeyList.class);
        PublicKey publicKey = getConstructorMonkey().giveMeOne(PublicKey.class);
        DefaultClaims claims = getConstructorMonkey().giveMeBuilder(DefaultClaims.class)
                .set("sub", Arbitraries.strings().withChars('a', 'z').ofMinLength(5).ofMaxLength(10).sample())
                .sample();

        doReturn(oidcPublicKeyList).when(kakaoAuthClient).getPublicKeys();
        doReturn(publicKey).when(publicKeyProvider).generatePublicKey(any(), any());
        doReturn(claims).when(jwtProvider).parseClaims(idToken, publicKey);

        String result = kakaoAuthProvider.getProviderId(idToken);

        assertEquals(claims.getSubject(), result);

        verify(kakaoAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(1)).generatePublicKey(any(), any());
        verify(jwtProvider, times(1)).parseClaims(idToken, publicKey);
    }

    @Test
    @DisplayName("공개키 목록을 받을 때 에러가 발생하면, 서버 외부 예외 발생")
    void getPublicKeyException() {
        doThrow(new JwtException(ExceptionReturnCode.EXTERNAL_SERVER_ERROR)).when(kakaoAuthClient).getPublicKeys();

        assertThrows(
                JwtException.class,
                () -> kakaoAuthProvider.getProviderId(idToken),
                ExceptionReturnCode.EXTERNAL_SERVER_ERROR.getMessage()
        );

        verify(kakaoAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(0)).generatePublicKey(any(), any());
        verify(jwtProvider, times(0)).parseClaims(any(), any());
    }

    @Test
    @DisplayName("헤더 파싱을 할 수 없으면, 서버 내부 예외 발생")
    void parseHeaderException() {
        doThrow(new JwtException(ExceptionReturnCode.EXTERNAL_SERVER_ERROR)).when(kakaoAuthClient).getPublicKeys();
        String idToken = "headerParseException.payload.signature";

        assertThrows(
                JwtException.class,
                () -> kakaoAuthProvider.getProviderId(idToken),
                ExceptionReturnCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        verify(kakaoAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(0)).generatePublicKey(any(), any());
        verify(jwtProvider, times(0)).parseClaims(any(), any());
    }

}
