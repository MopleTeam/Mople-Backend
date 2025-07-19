package com.mople.global.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingContextManager {
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_USER_INFO = "userInfo";
    public static final String MDC_FEATURE = "feature";

    public void setRequestId() {
        MDC.put(MDC_REQUEST_ID, UUID.randomUUID().toString().substring(0, 8));
    }

    public void setUserInfo(Long userId) {
        MDC.put(MDC_USER_INFO, String.valueOf(userId));
    }

    public void setFeature(String feature) {
        MDC.put(MDC_FEATURE, feature);
    }

    public String getRequestId() {
        return MDC.get(MDC_REQUEST_ID);
    }

    public String getUserInfo() {
        return MDC.get(MDC_USER_INFO);
    }

    public void clear() {
        MDC.clear();
    }
}
