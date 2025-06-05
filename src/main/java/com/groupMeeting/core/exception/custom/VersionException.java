package com.groupMeeting.core.exception.custom;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class VersionException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public VersionException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }
}
