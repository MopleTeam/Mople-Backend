package com.mople.global.logging;

public interface LoggerAdapter {
    void logComplete(long executionTime);
    void logError(String message);
}
