package com.mople.core.config;

import com.mople.global.logging.logger.SlowQueryLogger;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {
    private final DataSourceProperties dataSourceProperties;
    private final long threshold;
    private final SlowQueryLogger slowQueryLogger;

    public DataSourceConfig(
            DataSourceProperties dataSourceProperties,
            @Value("${logging.thresholds.slow.query}") long threshold,
            SlowQueryLogger slowQueryLogger
    ) {
        this.dataSourceProperties = dataSourceProperties;
        this.threshold = threshold;
        this.slowQueryLogger = slowQueryLogger;
    }

    @Bean
    public DataSource realDataSource() {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public DataSource loggingDataSource(@Qualifier("realDataSource") DataSource realDataSource) {

        return ProxyDataSourceBuilder
                .create(realDataSource)
                .name("SlowQueryLogger")
                .listener(new QueryExecutionListener() {

                    @Override
                    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                        long elapsedTime = execInfo.getElapsedTime();

                        if (elapsedTime > threshold) {
                            String query = queryInfoList.stream()
                                    .map(QueryInfo::getQuery)
                                    .collect(Collectors.joining(System.lineSeparator()));

                            slowQueryLogger.logSlowQuery(query, elapsedTime);
                        }
                    }

                    @Override
                    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                    }
                })
                .build();
    }
}
