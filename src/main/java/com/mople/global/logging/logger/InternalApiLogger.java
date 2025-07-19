package com.mople.global.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.logging.LoggerMessage.*;

@Slf4j
@Component
public class InternalApiLogger extends AbstractApiLogger {
    
    public InternalApiLogger() {
        super(
            LoggerFactory.getLogger("internal-api"),
            INTERNAL_API_SUCCESS.getMessage(),
            INTERNAL_API_SLOW_RESPONSE.getMessage(),
            INTERNAL_API_FAIL.getMessage()
        );
    }
}
