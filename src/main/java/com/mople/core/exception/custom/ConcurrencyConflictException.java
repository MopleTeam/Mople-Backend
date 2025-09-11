package com.mople.core.exception.custom;

import com.mople.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class ConcurrencyConflictException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;
    private final Long currentVersion;

    public ConcurrencyConflictException(ExceptionReturnCode exceptionReturnCode, Long currentVersion) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
        this.currentVersion = currentVersion;
    }
}