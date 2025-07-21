package com.mople.core.exception.custom;

import com.mople.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class AsyncException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public AsyncException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }
}
