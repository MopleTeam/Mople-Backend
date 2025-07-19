package com.mople.core.filter;

import com.mople.auth.oauth.OAuthUserDetails;
import com.mople.auth.provider.impl.JwtProvider;

import com.mople.dto.request.user.AuthUserRequest;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.core.exception.custom.JwtException;
import com.mople.global.logging.LoggingContextManager;
import io.opencensus.trace.ContextManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final String ACCESS_HEADER;
    private final String GRANT_TYPE;
    private final LoggingContextManager loggingContextManager;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            @Value("${jwt.access-header}") String accessHeader,
            @Value("${jwt.grant-type}") String grantType,
            LoggingContextManager loggingContextManager
    ) {

        this.jwtProvider = jwtProvider;
        this.ACCESS_HEADER = accessHeader;
        this.GRANT_TYPE = grantType;
        this.loggingContextManager = loggingContextManager;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Optional<String> token = getTokensFromHeader(request, ACCESS_HEADER);

        token.ifPresent(it -> {
            Authentication authentication = jwtProvider.getAuthentication(replaceBearerToBlank(it));

            if (authentication.getPrincipal() instanceof OAuthUserDetails userDetails) {
                AuthUserRequest user = userDetails.getUser();
                loggingContextManager.setUserInfo(user.id());
            }

            log.info("user in = {}", authentication.getPrincipal());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> getTokensFromHeader(
            HttpServletRequest request,
            String header
    ) {
        return Optional.ofNullable(request.getHeader(header));
    }

    private String replaceBearerToBlank(String token) {
        final String suffix = GRANT_TYPE + " ";

        if (!token.startsWith(suffix)) {
            throw new JwtException(ExceptionReturnCode.NOT_EXIST_BEARER_SUFFIX);
        }

        return token.replace(suffix, "");
    }
}
