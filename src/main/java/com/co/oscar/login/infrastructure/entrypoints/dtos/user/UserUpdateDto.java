package com.co.oscar.login.infrastructure.entrypoints.dtos.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "DTO para la actualización de la información de un Usuario")
public record UserUpdateDto(
        @NotNull(message = "El ID es obligatorio para actualizar") // 🟢 CAMBIO AQUÍ: @NotNull en vez de @NotBlank
        @Schema(description = "Identificador único del usuario", example = "1")
        Long id,

        @NotBlank(message = "El nombre no puede estar vacío")
        @Schema(description = "Nombre completo del usuario", example = "Oscar Ivan Ospina")
        String name,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico inválido")
        @JsonAlias("email")
        @Schema(description = "Correo electrónico", example = "oscar@correo.com")
        String email,

        @Schema(description = "Contraseña opcional (dejar vacío si no se desea cambiar)", example = "Admin123!", nullable = true)
        String password, // 🟢 Sigue estando aquí por si se requiere mapear, pero sin @NotBlank ni @Size obligatorios

        @Schema(description = "Estado de actividad del usuario en el sistema", example = "true")
        Boolean isActive,

        @Schema(description = "Fecha y hora de registro", example = "2026-05-15T18:01:24")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última modificación", example = "2026-05-15T18:01:24")
        LocalDateTime updatedAt
) {
}