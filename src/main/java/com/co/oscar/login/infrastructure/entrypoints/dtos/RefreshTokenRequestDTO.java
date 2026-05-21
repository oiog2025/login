package com.co.oscar.login.infrastructure.entrypoints.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RefreshTokenRequestDTO {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
