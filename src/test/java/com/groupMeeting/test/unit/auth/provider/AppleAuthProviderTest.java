package com.groupMeeting.test.unit.auth.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupMeeting.auth.key.OidcPublicKeyList;
import com.groupMeeting.auth.provider.impl.AppleAuthProvider;
import com.groupMeeting.auth.provider.impl.JwtProvider;
import com.groupMeeting.auth.provider.PublicKeyProvider;
import com.groupMeeting.test.base.object.MockitoTest;
import com.groupMeeting.global.client.AppleAuthClient;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.core.exception.custom.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;

import net.jqwik.api.Arbitraries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AppleAuthProviderTest extends MockitoTest {
    @Mock
    private AppleAuthClient appleAuthClient;

    @Mock
    private PublicKeyProvider publicKeyProvider;

    @Mock
    private JwtProvider jwtProvider;

    private AppleAuthProvider appleAuthProvider;

    private final String idToken = "eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.payload.signature";

    @BeforeEach
    public void setUp(){
        appleAuthProvider = new AppleAuthProvider(
                appleAuthClient,
                jwtProvider,
                publicKeyProvider,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("idToken으로 제공자 id를 받는다")
    void getProvideId() {
        OidcPublicKeyList oidcPublicKeyList = getConstructorMonkey().giveMeOne(OidcPublicKeyList.class);
        PublicKey publicKey = getConstructorMonkey().giveMeOne(PublicKey.class);
        DefaultClaims claims = getConstructorMonkey().giveMeBuilder(DefaultClaims.class)
                .set("sub", Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(10).sample())
                .sample();

        doReturn(oidcPublicKeyList).when(appleAuthClient).getPublicKeys();
        doReturn(publicKey).when(publicKeyProvider).generatePublicKey(any(), any());
        doReturn(claims).when(jwtProvider).parseClaims(idToken, publicKey);

        String result = appleAuthProvider.getProviderId(idToken);

        assertEquals(claims.getSubject(), result);

        verify(appleAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(1)).generatePublicKey(any(), any());
        verify(jwtProvider, times(1)).parseClaims(idToken, publicKey);
    }

    @Test
    @DisplayName("공개키 목록을 받을 때 에러가 발생하면, 서버 외부 예외 발생")
    void getPublicKeyException() {
        doThrow(new JwtException(ExceptionReturnCode.EXTERNAL_SERVER_ERROR)).when(appleAuthClient).getPublicKeys();

        assertThrows(JwtException.class,
                () -> appleAuthProvider.getProviderId(idToken),
                ExceptionReturnCode.EXTERNAL_SERVER_ERROR.getMessage()
        );

        verify(appleAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(0)).generatePublicKey(any(), any());
        verify(jwtProvider, times(0)).parseClaims(any(), any());
    }

    @Test
    @DisplayName("헤더 파싱을 할 수 없으면, 서버 내부 예외 발생")
    void parseHeaderException() {
        String idToken = "parseHeaderException.payload.signature";

        assertThrows(JwtException.class,
                () -> appleAuthProvider.getProviderId(idToken),
                ExceptionReturnCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        verify(appleAuthClient, times(1)).getPublicKeys();
        verify(publicKeyProvider, times(0)).generatePublicKey(any(), any());
        verify(jwtProvider, times(0)).parseClaims(any(), any());
    }
}
