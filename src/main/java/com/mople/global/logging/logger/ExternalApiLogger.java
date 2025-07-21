package com.mople.global.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.logging.LoggerMessage.*;

@Slf4j
@Component
public class ExternalApiLogger extends AbstractApiLogger {
    
    public ExternalApiLogger() {
        super(
            LoggerFactory.getLogger("external-api"),
            EXTERNAL_API_SUCCESS.getMessage(),
            EXTERNAL_API_SLOW_RESPONSE.getMessage(),
            EXTERNAL_API_FAIL.getMessage()
        );
    }
}
