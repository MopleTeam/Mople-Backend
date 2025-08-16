package com.mople.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SensitiveLogger {
    private static final Set<String> SENSITIVE_FIELDS =
            Set.of(
                    "password", "pw", "token", "secret", "key", "credential",
                    "providerToken", "deviceToken", "refreshToken", "accessToken"
            );

    private static final Set<String> SENSITIVE_CLASS_NAMES =
            Set.of(
                    "AdminLoginRequest", "UserSignInRequest", "UserSignUpRequest",
                    "TokenCreateRequest", "UserDeviceRequest", "AuthUserRequest"
            );

    public String sensitiveArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return "[" + Arrays.stream(args)
                .map(this::sensitiveObject)
                .collect(Collectors.joining(", ")) + "]";
    }

    private String sensitiveObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof HttpServletRequest) {
            return "HttpServletRequest";
        }

        if (obj.getClass().getName().contains("MultipartFile")) {
            return "MultipartFile";
        }

        String className = obj.getClass().getSimpleName();

        if (isSensitiveClass(className)) {
            return className + "[MASKED]";
        }

        String objString = obj.toString();
        if (containsSensitiveData(objString)) {
            return className + "[CONTAINS_SENSITIVE_DATA]";
        }

        if (objString.length() > 200) {
            return objString.substring(0, 200) + "...";
        }

        return objString;
    }

    private boolean isSensitiveClass(String className) {
        return SENSITIVE_CLASS_NAMES.contains(className) ||
                className.toLowerCase().contains("auth") ||
                className.toLowerCase().contains("token") ||
                className.toLowerCase().contains("login") ||
                className.toLowerCase().contains("password");
    }

    private boolean containsSensitiveData(String str) {
        String lowerStr = str.toLowerCase();
        return SENSITIVE_FIELDS.stream()
                .anyMatch(field -> lowerStr.contains(field + "=") || lowerStr.contains(field + ":"));
    }
}