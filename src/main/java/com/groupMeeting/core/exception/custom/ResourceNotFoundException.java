package com.groupMeeting.core.exception.custom;

import com.groupMeeting.global.enums.ExceptionReturnCode;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final ExceptionReturnCode exceptionReturnCode;

    public ResourceNotFoundException(ExceptionReturnCode exceptionReturnCode) {
        super(exceptionReturnCode.getMessage());
        this.exceptionReturnCode = exceptionReturnCode;
    }

}
