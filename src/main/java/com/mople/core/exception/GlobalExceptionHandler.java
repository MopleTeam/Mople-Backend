package com.mople.core.exception;

import com.mople.core.exception.custom.*;
import com.mople.core.exception.response.ExceptionResponse;

import com.mople.global.logging.LoggingContextManager;
import com.mople.global.logging.logger.ExceptionLogger;
import io.jsonwebtoken.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ExceptionLogger exceptionLogger;
    private final LoggingContextManager loggingContextManager;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleEntityNotFoundException(ResourceNotFoundException e) {
        exceptionLogger.logClientError(e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

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
        exceptionLogger.logServerError(e.getStackTrace(), e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

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
        exceptionLogger.logServerError(e.getStackTrace(), e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

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
        exceptionLogger.logClientError(e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

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
        exceptionLogger.logServerError(e.getStackTrace(), e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

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
        exceptionLogger.logClientError(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
        loggingContextManager.clear();

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
        exceptionLogger.logServerError(e.getStackTrace(), e.getMessage(), HttpStatus.BAD_REQUEST.value());
        loggingContextManager.clear();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse<>(
                                HttpStatus.BAD_REQUEST.toString(),
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(CursorException.class)
    public ResponseEntity<ExceptionResponse<Object>> handleCursorException(CursorException e) {
        exceptionLogger.logClientError(e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(PolicyException.class)
    public ResponseEntity<ExceptionResponse<Object>> handlePolicyException(PolicyException e) {
        exceptionLogger.logClientError(e.getMessage(), e.getExceptionReturnCode().returnCode());
        loggingContextManager.clear();

        return ResponseEntity
                .status(e.getExceptionReturnCode().returnCode())
                .body(new ExceptionResponse<>(
                                e.getExceptionReturnCode().getCode(),
                                e.getExceptionReturnCode().getMessage(),
                                null
                        )
                );
    }

//        @ExceptionHandler(Exception.class)
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
