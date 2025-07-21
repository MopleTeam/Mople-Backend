package com.mople.core.exception;

import com.mople.global.logging.LoggingContextManager;
import jakarta.servlet.RequestDispatcher;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.Optional;

import static com.mople.global.enums.logging.LoggerKey.REQUEST_ID;
import static com.mople.global.enums.logging.LoggerKey.USER_INFO;

@RequiredArgsConstructor
@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    public static final String ORIGINAL_EXCEPTION = "originalException";

    private final ExceptionLoggerService exceptionLoggerService;
    private final LoggingContextManager loggingContextManager;

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        rebuildingMdc(webRequest);

        Throwable error = getError(webRequest);
        int status = (int) Optional.ofNullable(webRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE, WebRequest.SCOPE_REQUEST)).orElse(500);

        Throwable originalException = (Throwable) webRequest.getAttribute(ORIGINAL_EXCEPTION, WebRequest.SCOPE_REQUEST);

        try {
            return exceptionLoggerService.logAndBuildErrorResponse(error, originalException, status);

        } finally {
            loggingContextManager.clear();
        }
    }

    private static void rebuildingMdc(WebRequest webRequest) {
        String requestId = (String) webRequest.getAttribute(REQUEST_ID.getKey(), WebRequest.SCOPE_REQUEST);
        String userInfo = (String) webRequest.getAttribute(USER_INFO.getKey(), WebRequest.SCOPE_REQUEST);

        if (requestId != null) {
            MDC.put(REQUEST_ID.getKey(), requestId);
        }
        if (userInfo != null) {
            MDC.put(USER_INFO.getKey(), userInfo);
        }
    }
}
