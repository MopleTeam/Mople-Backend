package com.mople.global.event.data.logging;

public record SlowQueryEvent(
        String query,
        long executionTime
) {
}