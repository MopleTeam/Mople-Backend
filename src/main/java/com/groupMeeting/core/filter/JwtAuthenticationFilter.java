package com.groupMeeting.core.filter;

import com.groupMeeting.auth.provider.impl.JwtProvider;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.core.exception.custom.JwtException;
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

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            @Value("${jwt.access-header}") String accessHeader,
            @Value("${jwt.grant-type}") String grantType
    ) {
        this.jwtProvider = jwtProvider;
        this.ACCESS_HEADER = accessHeader;
        this.GRANT_TYPE = grantType;
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
