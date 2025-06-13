package com.groupMeeting.core.interceptor;

import com.groupMeeting.global.logging.LoggingContextManager;
import com.groupMeeting.global.logging.logger.InternalApiLogger;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.groupMeeting.global.enums.logging.LoggerKey.REQUEST_ID;
import static com.groupMeeting.global.enums.logging.LoggerKey.USER_INFO;
import static com.groupMeeting.global.enums.logging.LoggerMessage.INTERNAL_API_FAIL_WITH_NO_EXCEPTION;

@RequiredArgsConstructor
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTR = "startTime";
    public static final String ORIGINAL_EXCEPTION = "originalException";

    private final InternalApiLogger internalApiLogger;
    private final LoggingContextManager loggingContextManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTR, startTime);

        if (handler instanceof HandlerMethod handlerMethod) {
            Class<?> controllerClass = handlerMethod.getBeanType();

            Tag tag = controllerClass.getAnnotation(Tag.class);
            if (tag != null) {
                loggingContextManager.setFeature(tag.name());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long executionTime = System.currentTimeMillis() - startTime;

        int status = response.getStatus();

        if (ex != null) {
            request.setAttribute(REQUEST_ID.getKey(), loggingContextManager.getRequestId());
            request.setAttribute(USER_INFO.getKey(), loggingContextManager.getUserInfo());
            request.setAttribute(ORIGINAL_EXCEPTION, ex);

            internalApiLogger.logError(ex.getMessage());

        } else if (status >= 200 && status < 400) {
            internalApiLogger.logComplete(executionTime);
        } else {
            internalApiLogger.logError(INTERNAL_API_FAIL_WITH_NO_EXCEPTION.getMessage());
        }

        loggingContextManager.clear();
    }
}
