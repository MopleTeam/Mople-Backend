package com.groupMeeting.core.resolver;

import com.groupMeeting.core.annotation.auth.RefreshToken;
import com.groupMeeting.core.exception.custom.JwtException;

import org.jetbrains.annotations.NotNull;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;
import static org.springframework.util.StringUtils.hasLength;

public class RefreshTokenResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RefreshToken.class)
                && String.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public String resolveArgument(
            @NotNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        final String refreshToken = webRequest.getHeader("Refresh");

        if(!hasLength(refreshToken)){
            throw new JwtException(EMPTY_REFRESH);
        }

        return refreshToken;
    }
}
