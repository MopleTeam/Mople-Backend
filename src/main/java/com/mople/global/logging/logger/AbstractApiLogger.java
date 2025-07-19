package com.mople.global.logging.logger;

import com.mople.global.logging.LoggerAdapter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import static com.mople.global.enums.logging.LoggerKey.*;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

public abstract class AbstractApiLogger implements LoggerAdapter {

    protected final Logger logger;
    protected final String successMessage;
    protected final String slowMessage;
    protected final String failMessage;

    @Value("${logging.thresholds.slow.api:3000}")
    protected int slowThreshold;

    protected AbstractApiLogger(Logger logger, String successMessage, String slowMessage, String failMessage) {
        this.logger = logger;
        this.successMessage = successMessage;
        this.slowMessage = slowMessage;
        this.failMessage = failMessage;
    }

    @Override
    public void logComplete(long executionTime) {
        if (executionTime > slowThreshold) {
            logSlow(executionTime);

            return;
        }

        logSuccess(executionTime);
    }

    @Override
    public void logError(String message) {

        logger.warn(
                failMessage,
                keyValue(ERROR_MESSAGE.getKey(), message),
                keyValue(SUCCESS.getKey(), false)
        );
    }

    protected void logSuccess(long executionTime) {

        if (logger.isInfoEnabled()) {
            logger.info(
                    successMessage,
                    keyValue(EXECUTION_TIME.getKey(), executionTime),
                    keyValue(SUCCESS.getKey(), true)
            );
        }
    }

    protected void logSlow(long executionTime) {
        logger.warn(
                slowMessage,
                keyValue(EXECUTION_TIME.getKey(), executionTime),
                keyValue(SUCCESS.getKey(), true)
        );
    }
}