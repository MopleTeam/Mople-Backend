package com.mople.global.event.notifier.support;

import org.springframework.stereotype.Component;

@Component
public class StackTracePreviewer {

    public String preview(Throwable ex) {
        if (ex == null) return "exception is null";

        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) return "stack trace empty";

        int limit = Math.min(5, stackTrace.length);
        StringBuilder preview = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            preview.append(stackTrace[i]).append("\n");
        }

        if (stackTrace.length > limit) {
            preview.append("... and ").append(stackTrace.length - limit).append(" more");
        }

        return preview.toString();
    }
}
