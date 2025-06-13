package com.groupMeeting.global.logging.logger;

import com.groupMeeting.global.logging.LoggerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.groupMeeting.global.enums.logging.LoggerKey.*;
import static com.groupMeeting.global.enums.logging.LoggerMessage.*;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
public class BusinessLogicLogger implements LoggerAdapter {
    private static final Logger businessLogicLogger = LoggerFactory.getLogger("business-logic");

    @Override
    public void logComplete(long executionTime) {
        if (executionTime > 1000) {
            businessLogicLogger.warn(BUSINESS_LOGIC_SLOW_RESPONSE.name(),
                    keyValue(EXECUTION_TIME.getKey(), executionTime),
                    keyValue(SUCCESS.getKey(), true));
        } else {
            businessLogicLogger.info(BUSINESS_LOGIC_SUCCESS.getMessage(),
                    keyValue(EXECUTION_TIME.getKey(), executionTime),
                    keyValue(SUCCESS.getKey(), true));
        }
    }

    @Override
    public void logError(String message) {
        businessLogicLogger.warn(BUSINESS_LOGIC_FAIL.getMessage(),
                keyValue(ERROR_MESSAGE.getKey(), message),
                keyValue(SUCCESS.getKey(), false));
    }
}
