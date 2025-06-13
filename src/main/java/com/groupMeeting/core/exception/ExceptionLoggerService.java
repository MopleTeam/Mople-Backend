package com.groupMeeting.core.exception;

import com.groupMeeting.global.logging.logger.ExceptionLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.groupMeeting.global.enums.logging.ErrorMessage.*;

@RequiredArgsConstructor
@Service
public class ExceptionLoggerService {
    private final ExceptionLogger exceptionLogger;

    public Map<String, Object> logAndBuildErrorResponse(Throwable error, Throwable originalException, int status) {
        String message = createMessage(status);

        if (status >= 400 && status < 500) {
            exceptionLogger.logClientError(message, status);
        } else {
            Throwable logError = originalException != null ? originalException : error;
            exceptionLogger.logServerError(logError.getStackTrace(), message, status);
        }

        return getErrorAttributes(status, message);
    }

    private static Map<String, Object> getErrorAttributes(int status, String message) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("code", status);
        errorAttributes.put("message", message);
        errorAttributes.put("data", null);
        return errorAttributes;
    }

    private String createMessage(int status) {
        return switch (status) {
            case 404 -> MESSAGE_404.getMessage();
            case 403 -> MESSAGE_403.getMessage();
            default -> switch (status / 100) {
                case 4 -> MESSAGE_4XX.getMessage();
                case 5 -> MESSAGE_5XX.getMessage();
                default -> MESSAGE_DEFAULT.getMessage();
            };
        };
    }
}
