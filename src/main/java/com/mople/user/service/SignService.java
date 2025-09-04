package com.mople.user.service;

import com.mople.auth.factory.AuthProviderFactory;
import com.mople.auth.provider.impl.JwtProvider;
import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.JwtException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.request.user.UserSignInRequest;
import com.mople.dto.request.user.UserSignUpRequest;
import com.mople.dto.response.token.TokenResponse;
import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Role;
import com.mople.global.enums.Status;
import com.mople.notification.repository.FirebaseTokenRepository;
import com.mople.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class SignService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AuthProviderFactory authProviderFactory;
    private final FirebaseTokenRepository firebaseTokenRepository;

    @Transactional
    public TokenResponse signUp(final UserSignUpRequest sign) {
        if (isNull(authProviderFactory.getAuthProviderId(sign.socialProvider(), sign.providerToken()))) {
            throw new AuthException(ExceptionReturnCode.TOKEN_NOT_VALID);
        }

        final User user = User.builder()
                .email(sign.email())
                .nickname(sign.nickname())
                .profileImg(sign.image())
                .socialProvider(sign.socialProvider())
                .lastLaunchAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        User createUser = userRepository.save(user);

        return jwtProvider.generateToken(new AuthUserRequest(createUser.getId(), createUser.getRole()));
    }

    @Transactional
    public TokenResponse signIn(final UserSignInRequest sign) {
        if (isNull(authProviderFactory.getAuthProviderId(sign.socialProvider(), sign.providerToken()))) {
            throw new AuthException(ExceptionReturnCode.TOKEN_NOT_VALID);
        }

        final User user = userRepository.loginCheck(sign.email(), Status.ACTIVE)
                .orElseThrow(() -> new AuthException(ExceptionReturnCode.NOT_USER));

        if (!user.getSocialProvider().equals(sign.socialProvider())) {
            throw new AuthException(ExceptionReturnCode.ANOTHER_PROVIDER);
        }

        firebaseTokenRepository.findByUserId(user.getId()).ifPresent(FirebaseToken::activeToken);

        return jwtProvider.generateToken(new AuthUserRequest(user.getId(), user.getRole()));
    }

    public TokenResponse recreateToken(final String refreshToken) {
        final AuthUserRequest user = jwtProvider.getUser(refreshToken);

        if (jwtProvider.expired(refreshToken)) {
            firebaseTokenRepository.delete(
                    firebaseTokenRepository.findByUserId(user.id())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_FIREBASE_TOKEN)
                            )
            );

            throw new JwtException(ExceptionReturnCode.EXPIRED_REFRESH_TOKEN);
        }

        return jwtProvider.recreateAccessToken(user, refreshToken);
    }

    @Transactional
    public void signOut(Long userId) {
        firebaseTokenRepository.findByUserId(userId).ifPresent(FirebaseToken::inActiveToken);
    }
}