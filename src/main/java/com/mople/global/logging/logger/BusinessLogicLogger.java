package com.mople.global.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.logging.LoggerMessage.*;

@Slf4j
@Component
public class BusinessLogicLogger extends AbstractApiLogger {
    
    public BusinessLogicLogger() {
        super(
            LoggerFactory.getLogger("business-logic"),
            BUSINESS_LOGIC_SUCCESS.getMessage(),
            BUSINESS_LOGIC_SLOW_RESPONSE.getMessage(),
            BUSINESS_LOGIC_FAIL.getMessage()
        );
    }
}
