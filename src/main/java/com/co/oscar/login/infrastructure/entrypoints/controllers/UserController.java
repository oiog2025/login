package com.co.oscar.login.infrastructure.entrypoints.controllers;

import com.co.oscar.login.application.ports.input.UserInPort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.domain.exceptions.UserNotFoundException;
import com.co.oscar.login.infrastructure.entrypoints.dtos.*;
import com.co.oscar.login.infrastructure.entrypoints.dtos.login.RefreshTokenRequestDTO;
import com.co.oscar.login.infrastructure.entrypoints.dtos.login.TokenResponseDTO;
import com.co.oscar.login.infrastructure.entrypoints.dtos.user.UserDto;
import com.co.oscar.login.infrastructure.entrypoints.dtos.user.UserRequestDTO;
import com.co.oscar.login.infrastructure.entrypoints.dtos.user.UserUpdateDto;
import com.co.oscar.login.infrastructure.mapper.UserMapper;
import com.co.oscar.login.infrastructure.security.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación y Usuarios", description = "Controlador encargado de la gestión de usuarios y procesos de autenticación/sesión.")
public class UserController {

    private final UserInPort userInPort;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final TokenServicePort tokenServicePort;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con sus credenciales y retorna un access token junto a un refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas / No autorizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<TokenResponseDTO>> login(@Valid @RequestBody UserRequestDTO request) {
        return userInPort.loginWithRefreshToken(request.getUsername(), request.getPassword())
                .map(loginResponse -> {
                    TokenResponseDTO tokens = new TokenResponseDTO(
                            loginResponse.getAccessToken(),
                            loginResponse.getRefreshToken()
                    );
                    return ResponseEntity.ok(ApiResponseDto.success(tokens, "Login successful"));
                })
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials. Please check your email and password."));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca y deshabilita el refresh token activo del usuario para cerrar su sesión de forma segura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponseDto.success(null, "Logout successful"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo access token y rota el refresh token utilizando un refresh token válido y activo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, revocado o expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<TokenResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String tokenStr = request.getRefreshToken();

        if (!refreshTokenService.isTokenValid(tokenStr)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = refreshTokenService.getUsernameFromToken(tokenStr);
        refreshTokenService.revokeToken(tokenStr);

        String newAccessToken = tokenServicePort.generateToken(username, Map.of("user", username));
        String newRefreshTokenStr = refreshTokenService.createRefreshToken(username);

        return ResponseEntity.ok(ApiResponseDto.success(
                new TokenResponseDTO(newAccessToken, newRefreshTokenStr),
                "Token refreshed successfully"
        ));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear un nuevo usuario", description = "Registra un nuevo usuario en el sistema a partir de los datos proporcionados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud o datos inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<UserDto>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del usuario a crear")
            @Valid @RequestBody UserDto userDTO) {
        User userDomain = userMapper.toDomain(userDTO);
        Optional<User> createdUser = userInPort.createUser(userDomain);
        return createdUser
                .map(user -> ResponseEntity.ok(ApiResponseDto.success(userMapper.toDto(user), "User created successfully")))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Busca y retorna la información de un usuario específico mediante su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no existe en el sistema o se encuentra inactivo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<UserDto>> getUserById(@PathVariable Long id) {
        return userInPort.getUser(id)
                .map(user -> ResponseEntity.ok(ApiResponseDto.success(userMapper.toDto(user), "User found successfully")))
                .orElseThrow(() -> new UserNotFoundException("El usuario con ID " + id + " no existe en el sistema o no esta activo."));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "No se pudo actualizar debido a que el usuario no existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<UserDto>> updateUser(@Valid @RequestBody UserUpdateDto userUpdateDTO) {
        User userDomain = userMapper.toDomain(userUpdateDTO);
        return userInPort.updateUser(userDomain)
                .map(user -> ResponseEntity.ok(ApiResponseDto.success(userMapper.toDto(user), "User updated successfully")))
                .orElseThrow(() -> new UserNotFoundException("No se pudo actualizar. El usuario no existe en el sistema."));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema usando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long id) {
        userInPort.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDto.success(null, "User deleted successfully"));
    }

    @GetMapping("/users")
    @Operation(
            summary = "Obtener todos los usuarios",
            description = "Retorna una lista con todos los usuarios registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            // Nota: Al retornar una lista, envolvemos el UserDto en un array dentro del Schema
                            schema = @Schema(implementation = ApiResponseDto.class)
                    )
            )
    })
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getAllUsers() {
        Optional<List<User>> domainUsersOptional = userInPort.getAllUser();
        List<User> domainUsers = domainUsersOptional.orElse(Collections.emptyList());
        List<UserDto> userDtos = userMapper.toDtoList(domainUsers);
        return ResponseEntity.ok(ApiResponseDto.success(userDtos, "Users retrieved successfully"));
    }

}
