package com.mople.global.event.data.exception;

import com.mople.global.event.data.Define;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class ErrorAlertMessage implements Define {
    String trace;
    @Nullable
    String contextPath;
    @Nullable
    String requestUrl;
    @Nullable
    String method;
    @Nullable
    Map<String, String[]> parameterMap;
    @Nullable
    String remoteAddress;
    @Nullable
    String header;
}
