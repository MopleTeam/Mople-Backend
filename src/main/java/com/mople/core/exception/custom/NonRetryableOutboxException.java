package com.mople.core.exception.custom;

import com.mople.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class NonRetryableOutboxException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public NonRetryableOutboxException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }

}
