package com.groupMeeting.global.logging.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.groupMeeting.global.enums.logging.LoggerMessage.SLOW_QUERY_DETECTED;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
public class SlowQueryLogger {
    private static final Logger slowQueryLogger = LoggerFactory.getLogger("slow-query");

    public void logSlowQuery(String query, long executionTime) {
        String querySubstr = query.length() > 500 ? query.substring(0, 500) + "..." : query;

        slowQueryLogger.warn(SLOW_QUERY_DETECTED.getMessage(),
                keyValue("query", querySubstr),
                keyValue("executionTime", executionTime));
    }
}
