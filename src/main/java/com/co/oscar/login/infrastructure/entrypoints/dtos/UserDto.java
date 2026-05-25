package com.co.oscar.login.infrastructure.entrypoints.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "DTO que representa la información de transferencia para un Usuario")
public record UserDto(
        @Schema(description = "Identificador único del usuario (No enviar en la creación)", example = "null", nullable = true)
        Long id,

        @NotBlank(message = "El nombre no puede estar vacío")
        @Schema(description = "Nombre completo del usuario", example = "Oscar Ivan Ospina")
        String name,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico inválido")
        @JsonAlias("email")
        @Schema(description = "Correo electrónico institucional o personal", example = "oscar@correo.com")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(description = "Contraseña segura de acceso", example = "Admin123!")
        String password,

        @Schema(description = "Estado de actividad del usuario en el sistema", example = "true")
        Boolean isActive,

        @Schema(description = "Fecha y hora de registro", example = "2026-05-15T18:01:24")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última modificación", example = "2026-05-15T18:01:24")
        LocalDateTime updatedAt
) {
}