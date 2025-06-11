package com.groupMeeting.core.exception.custom;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class PolicyException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public PolicyException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }
}
