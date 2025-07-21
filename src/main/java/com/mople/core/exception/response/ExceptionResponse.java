package com.mople.core.exception.response;

public record ExceptionResponse<T>(String code, String message, T data) {
}
