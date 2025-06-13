package com.groupMeeting.global.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.groupMeeting.global.enums.logging.LoggerKey.*;
import static com.groupMeeting.global.enums.logging.LoggerMessage.EXCEPTION_CLIENT;
import static com.groupMeeting.global.enums.logging.LoggerMessage.EXCEPTION_SERVER;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
public class ExceptionLogger {
    private static final Logger exceptionLogger = LoggerFactory.getLogger("exception");
    private static final int STACK_TRACE_LENGTH = 40;

    public void logServerError(StackTraceElement[] stackTraceElements, String message, int status) {
        int limit = Math.min(STACK_TRACE_LENGTH, stackTraceElements.length);
        StringBuilder stackTrace = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            stackTrace.append(stackTraceElements[i].toString()).append(System.lineSeparator());
        }

        exceptionLogger.error(EXCEPTION_SERVER.getMessage(),
                keyValue(STATUS.getKey(), status),
                keyValue(ERROR_MESSAGE.getKey(), message),
                keyValue(STACK_TRACE.getKey(), stackTrace.toString())
        );
    }

    public void logClientError(String message, int status) {
        exceptionLogger.warn(EXCEPTION_CLIENT.getMessage(),
                keyValue(STATUS.getKey(), status),
                keyValue(ERROR_MESSAGE.getKey(), message)
        );
    }
}
