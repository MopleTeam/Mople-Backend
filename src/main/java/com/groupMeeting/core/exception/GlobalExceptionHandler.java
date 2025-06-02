package com.groupMeeting.core.exception;

import com.groupMeeting.core.exception.custom.*;
import com.groupMeeting.core.exception.response.ExceptionResponse;

import io.jsonwebtoken.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleEntityNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleJwtExceptionException(JwtException e) {
        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleAuthExceptionException(AuthException e) {
        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleBadRequestException(BadRequestException e) {
        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(FileHandleException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleFileException(FileHandleException e) {
        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleValidationException(HandlerMethodValidationException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ExceptionResponse<>(
                                HttpStatus.UNPROCESSABLE_ENTITY.toString(),
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleIOException(IOException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse<>(
                                HttpStatus.BAD_REQUEST.toString(),
                                e.getMessage(),
                                null
                        )
                );
    }

    //    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ExceptionResponse<Object>> handleOtherException(Exception e) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ExceptionResponse<>(
//                                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
//                                String.format("%s with stack trace: %s", e.getMessage(), getStackTraceConvertString(e)),
//                                null
//                        )
//                );
//    }

    private String getStackTraceConvertString(Exception e) {
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString()).append("\n");
        }

        return sb.toString();
    }
}
