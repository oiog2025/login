package com.co.oscar.login.infrastructure.entrypoints.dtos;

import java.time.LocalDateTime;

public record ApiResponseDto<T>(
        boolean success,
        T data,
        String message,
        LocalDateTime timestamp
) {

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>(true, data, message, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, null, message, LocalDateTime.now());
    }
}
