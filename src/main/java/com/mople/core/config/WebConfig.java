package com.mople.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import okhttp3.OkHttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /*
     * ObjectMapper에서 LocalDateTime을 직렬화, 역직렬화 시 불필요한 정보가 있음
     * 필요한 정보만 역직렬화하기위해 ObjectMapper Bean으로 등록
     *
     * ObjectMapper는 생성비용이 크다 -> Bean으로 생성 후 재사용
     *
     * FAIL_ON_UNKNOWN_PROPERTIES -> 정의되지 않은 프로퍼티는 무시
     * */
    @Bean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateFormat)));
        javaTimeModule.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateFormat))
        );

        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    @Bean
    public OkHttpClient okHttp() {
        return new OkHttpClient.Builder().build();
    }
}
