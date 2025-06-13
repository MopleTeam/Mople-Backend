package com.groupMeeting.user.service;

import com.groupMeeting.auth.factory.AuthProviderFactory;
import com.groupMeeting.auth.provider.impl.JwtProvider;
import com.groupMeeting.core.exception.custom.AuthException;
import com.groupMeeting.core.exception.custom.JwtException;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.request.user.UserSignInRequest;
import com.groupMeeting.dto.request.user.UserSignUpRequest;
import com.groupMeeting.dto.response.token.TokenResponse;
import com.groupMeeting.entity.notification.FirebaseToken;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.enums.Role;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.notification.repository.FirebaseTokenRepository;
import com.groupMeeting.user.repository.UserRepository;

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
                .status(Status.ACTIVE)
                .build();

        User createUser = userRepository.save(user);

        return jwtProvider.generateToken(new AuthUserRequest(createUser.getId(), createUser.getRole()));
    }

    @Transactional
    public TokenResponse signIn(final UserSignInRequest sign) {
        if (isNull(authProviderFactory.getAuthProviderId(sign.socialProvider(), sign.providerToken()))) {
            throw new AuthException(ExceptionReturnCode.TOKEN_NOT_VALID);
        }

        final User user = userRepository.loginCheck(sign.email())
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