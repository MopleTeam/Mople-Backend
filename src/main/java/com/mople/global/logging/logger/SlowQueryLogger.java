package com.mople.global.logging.logger;

import com.mople.global.event.data.logging.SlowQueryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.logging.LoggerMessage.SLOW_QUERY_DETECTED;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlowQueryLogger {

    private static final Logger slowQueryLogger = LoggerFactory.getLogger("slow-query");
    private final ApplicationEventPublisher eventPublisher;

    public void logSlowQuery(String query, long executionTime) {
        String normalizedQuery = query.replaceAll("\\s+", " ").trim();
        String querySubstr = normalizedQuery.length() > 500 ? normalizedQuery.substring(0, 500) + "..." : normalizedQuery;

        slowQueryLogger.warn(
                SLOW_QUERY_DETECTED.getMessage(),
                keyValue("query", querySubstr),
                keyValue("executionTime", executionTime)
        );

        eventPublisher.publishEvent(new SlowQueryEvent(querySubstr, executionTime));
    }
}
