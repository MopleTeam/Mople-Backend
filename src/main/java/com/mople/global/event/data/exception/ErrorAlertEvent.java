package com.mople.global.event.data.exception;

import jakarta.servlet.http.HttpServletRequest;

public record ErrorAlertEvent(
        Exception exception,
        HttpServletRequest request
) {
}