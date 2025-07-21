package com.mople.core.filter;

import com.mople.global.logging.LoggingContextManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {
    private final LoggingContextManager loggingContextManager;

    private static final String[] whitelist = {
            // image
            "/favicon.ico",
            // swagger
            "/docs", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**",
            // admin path
            "/v1/**"
    };

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        loggingContextManager.setRequestId();

        try {
            chain.doFilter(request, response);
        } finally {
            loggingContextManager.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        return PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}
