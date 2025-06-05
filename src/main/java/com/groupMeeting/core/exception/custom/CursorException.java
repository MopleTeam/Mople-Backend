package com.groupMeeting.core.exception.custom;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class CursorException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public CursorException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }
}
