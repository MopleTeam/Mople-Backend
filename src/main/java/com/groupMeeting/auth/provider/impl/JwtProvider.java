package com.groupMeeting.auth.provider.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.groupMeeting.auth.oauth.OAuthUserDetails;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.response.token.TokenResponse;
import com.groupMeeting.core.exception.custom.JwtException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.PublicKey;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class JwtProvider {
    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;
    private final Long accessTokenExpired;
    private final Long refreshTokenExpired;
    private final String userKey;

    public JwtProvider(
            ObjectMapper objectMapper,
            SecretKey secretKey,
            @Value("${jwt.expire.access}") Long accessTokenExpired,
            @Value("${jwt.expire.refresh}") Long refreshTokenExpired,
            @Value("${jwt.user-key}") String userKey
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
        this.accessTokenExpired = accessTokenExpired;
        this.refreshTokenExpired = refreshTokenExpired;
        this.userKey = userKey;
    }

    public TokenResponse generateToken(final AuthUserRequest user) {
        if (isNull(user)) {
            throw new JwtException(EMPTY_USER);
        }

        try {
            final String accessToken =
                    Jwts.builder()
                            .claim(userKey, user)
                            .expiration(new Date(System.currentTimeMillis() + accessTokenExpired))
                            .signWith(secretKey, Jwts.SIG.HS512)
                            .compact();

            final String refreshToken =
                    Jwts.builder()
                            .claim(userKey, user)
                            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpired))
                            .signWith(secretKey, Jwts.SIG.HS512)
                            .compact();

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (InvalidKeyException e) {
            throw new JwtException(INVALID_KEY);
        }
    }

    public TokenResponse recreateAccessToken(final AuthUserRequest user, String refreshToken) {
        if (isNull(user)) {
            throw new JwtException(EMPTY_USER);
        }

        try {
            if (isSameDate(refreshToken)) {
                return generateToken(user);
            }

            final String accessToken =
                    Jwts.builder()
                            .claim(userKey, user)
                            .expiration(new Date(System.currentTimeMillis() + accessTokenExpired))
                            .signWith(secretKey, Jwts.SIG.HS512)
                            .compact();

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (InvalidKeyException e) {
            throw new JwtException(INVALID_KEY);
        }
    }


    public Authentication getAuthentication(String token) {
        final AuthUserRequest user = getUser(token);
        final OAuthUserDetails userDetails = new OAuthUserDetails(user);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public AuthUserRequest getUser(String token) {
        final Claims claims = parseClaims(token);

        if (isNull(claims.get(userKey))) {
            throw new JwtException(EMPTY_AUTH_JWT);
        }

        return objectMapper.convertValue(claims.get(userKey), AuthUserRequest.class);
    }

    public boolean expired(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new JwtException(EXPIRED_JWT_TOKEN);
        } catch (RuntimeException e) {
            throw new JwtException(WRONG_JWT_TOKEN);
        }
    }

    public Claims parseClaims(String token, PublicKey publicKey) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new JwtException(EXPIRED_JWT_TOKEN);
        } catch (RuntimeException e) {
            throw new JwtException(WRONG_JWT_TOKEN);
        }
    }

    private boolean isSameDate(String token) {
        return LocalDate.ofInstant(
                        parseClaims(token).getExpiration().toInstant(),
                        ZoneId.systemDefault()
                )
                .equals(LocalDate.now());
    }
}