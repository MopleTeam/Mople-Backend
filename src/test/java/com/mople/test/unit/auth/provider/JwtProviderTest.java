package com.mople.test.unit.auth.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mople.auth.provider.impl.JwtProvider;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.token.TokenResponse;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.core.exception.custom.JwtException;
import com.mople.test.base.object.MockitoTest;
import com.mople.core.config.JwtConfig;
import com.mople.test.base.util.TestUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@Import(JwtConfig.class)
public class JwtProviderTest extends MockitoTest {
    @Mock
    private SecretKey secretKey;

    private final JwtProvider jwtProvider = new JwtProvider(
            new ObjectMapper(),
            secretKey,
            TestUtil.ACCESS_TOKEN_EXPIRED_SECOND,
            TestUtil.REFRESH_TOKEN_EXPIRED_SECOND,
            TestUtil.USER_KEY
    );


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                jwtProvider, "secretKey", Keys.hmacShaKeyFor(Decoders.BASE64.decode(TestUtil.SECRET_KEY))
        );
    }

    @Test
    @DisplayName("토큰 생성에 성공")
    void generateJwt() {
        AuthUserRequest user = getConstructorMonkey().giveMeOne(AuthUserRequest.class);

        TokenResponse jwt = jwtProvider.generateToken(user);

        assertNotNull(jwt.accessToken());
        assertNotNull(jwt.refreshToken());
    }

    @Test
    @DisplayName("유저가 null이면 토큰 생성 실패")
    void failGenerateJwt() {
        assertThrows(
                JwtException.class,
                () -> jwtProvider.generateToken(null),
                ExceptionReturnCode.EMPTY_USER.getMessage()
        );
    }

    @Test
    @DisplayName("토큰으로부터 인증 정보 확인")
    void getAuthentication() {
        AuthUserRequest user = getConstructorMonkey().giveMeOne(AuthUserRequest.class);

        String accessToken = jwtProvider.generateToken(user).accessToken();

        Authentication authentication = jwtProvider.getAuthentication(accessToken);

        assertEquals(authentication.getAuthorities().toString(), "[%s]".formatted(user.securityRole()));
    }

    @Test
    @DisplayName("토큰으로 부터 Claim 정보 확인")
    void parseClaims() {
        AuthUserRequest user = getConstructorMonkey().giveMeOne(AuthUserRequest.class);

        String accessToken = jwtProvider.generateToken(user).accessToken();

        Claims claims = jwtProvider.parseClaims(accessToken);

        assertNotNull(claims);
    }

    @Test
    @DisplayName("만료 날짜 가져오기")
    void parseClaimTime() {
        AuthUserRequest user = getConstructorMonkey().giveMeOne(AuthUserRequest.class);

        String refreshToken = jwtProvider.generateToken(user).refreshToken();

        Claims claims = jwtProvider.parseClaims(refreshToken);
        System.out.println(LocalDate.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault()).equals(LocalDate.now()));

        assertNotNull(claims);
    }
}
