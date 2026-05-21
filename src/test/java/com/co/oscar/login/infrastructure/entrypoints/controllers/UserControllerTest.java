package com.co.oscar.login.infrastructure.entrypoints.controllers;

import com.co.oscar.login.application.ports.input.UserInPort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.infrastructure.entrypoints.dtos.UserDto;
import com.co.oscar.login.infrastructure.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Importaciones estáticas para mantener el código limpio y legible

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class) // 💡 CRÍTICO: Importamos el manejador para que intercepte los errores en los tests
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserInPort userInPort;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private TokenServicePort tokenServicePort;

    @Nested
    @DisplayName("Pruebas para el Endpoint POST /api/auth/create")
    class CreateUserTests {
        @Test
        @DisplayName("Debe retornar 200 OK y ApiResponseDto exitoso cuando los datos son correctos")
        void shouldCreateUserSuccessfully() throws Exception {
            UserDto inputDto = new UserDto(null, "Oscar Ivan Ospina", "oscar@correo.com", "Admin123!", true, null, null);
            User domainUser = new User(null, "Oscar Ivan Ospina", "Admin123!", true, "oscar@correo.com", null, null);
            User savedUser = new User(1L, "Oscar Ivan Ospina", "hash_encrypted", true, "oscar@correo.com", LocalDateTime.now(), LocalDateTime.now());
            UserDto outputDto = new UserDto(1L, "Oscar Ivan Ospina", "oscar@correo.com", null, true, null, null);

            when(userMapper.toDomain(any(UserDto.class))).thenReturn(domainUser);
            when(userInPort.createUser(any(User.class))).thenReturn(Optional.of(savedUser));
            when(userMapper.toDto(any(User.class))).thenReturn(outputDto);

            mockMvc.perform(post("/api/auth/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("oscar@correo.com"));
        }
    }

    @Nested
    @DisplayName("Pruebas para el Endpoint POST /api/auth/login")
    class LoginTests {
        @Test
        @DisplayName("Debe retornar 200 OK y el Token envuelto cuando las credenciales son correctas")
        void shouldLoginSuccessfully() throws Exception {
            String requestJson = "{\"username\":\"oscar@correo.com\",\"password\":\"Admin123!\"}";
            String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummytoken.firma";

            when(userInPort.login("oscar@correo.com", "Admin123!")).thenReturn(Optional.of(fakeToken));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.data.token").value(fakeToken));
        }

        @Test
        @DisplayName("Debe retornar 401 Unauthorized y un error cuando las credenciales fallan")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            String requestJson = "{\"username\":\"oscar@correo.com\",\"password\":\"ClaveEquivocada\"}";

            when(userInPort.login(any(String.class), any(String.class))).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid credentials. Please check your username and password."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("Pruebas para el Endpoint GET /api/auth/users/{id}")
    class GetUserByIdTests {
        @Test
        @DisplayName("Debe retornar 200 OK y el usuario cuando el ID existe")
        void shouldReturn200AndUserWhenUserExists() throws Exception {
            Long targetId = 1L;
            User domainUser = new User(targetId, "Oscar Ivan Ospina", "Admin123!", true, "oscar@correo.com", null, null);
            UserDto outputDto = new UserDto(targetId, "Oscar Ivan Ospina", "oscar@correo.com", null, true, null, null);

            when(userInPort.getUser(targetId)).thenReturn(Optional.of(domainUser));
            when(userMapper.toDto(any(User.class))).thenReturn(outputDto);

            mockMvc.perform(get("/api/auth/users/{id}", targetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User found successfully"))
                    .andExpect(jsonPath("$.data.id").value(targetId))
                    .andExpect(jsonPath("$.data.email").value("oscar@correo.com"));
        }

        @Test
        @DisplayName("Debe retornar 404 Not Found cuando el ID NO existe")
        void shouldReturn404WhenUserDoesNotExist() throws Exception {
            Long fakeId = 999L;
            when(userInPort.getUser(fakeId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/auth/users/{id}", fakeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Pruebas para el Endpoint DELETE /api/auth/users/{id}")
    class DeleteUserTests {
        @Test
        @DisplayName("Debe retornar 200 OK y ApiResponseDto exitoso al eliminar un usuario")
        void shouldDeleteUserSuccessfully() throws Exception {
            Long targetId = 1L;

            mockMvc.perform(delete("/api/auth/users/{id}", targetId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User deleted successfully"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Debe retornar 404 Not Found cuando el ID a eliminar no existe")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            Long fakeId = 999L;
            doThrow(new com.co.oscar.login.domain.exceptions.UserNotFoundException("El usuario con ID " + fakeId + " no existe en el sistema."))
                    .when(userInPort).deleteUser(fakeId);

            mockMvc.perform(delete("/api/auth/users/{id}", fakeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("El usuario con ID 999 no existe en el sistema."));
        }
    }

    @Nested
    @DisplayName("Pruebas para el Endpoint PUT /api/auth/update")
    class UpdateUserTests {
        @Test
        @DisplayName("Debe retornar 200 OK y ApiResponseDto exitoso al actualizar un usuario")
        void shouldUpdateUserSuccessfully() throws Exception {
            UserDto inputDto = new UserDto(1L, "Oscar Actualizado", "oscar@correo.com", "NewPass123!", true, null, null);
            User domainUser = new User(1L, "Oscar Actualizado", "NewPass123!", true, "oscar@correo.com", null, null);
            User updatedUser = new User(1L, "Oscar Actualizado", "hashed_new_pass", true, "oscar@correo.com", LocalDateTime.now(), LocalDateTime.now());
            UserDto outputDto = new UserDto(1L, "Oscar Actualizado", "oscar@correo.com", null, true, null, null);

            when(userMapper.toDomain(any(UserDto.class))).thenReturn(domainUser);
            when(userInPort.updateUser(any(User.class))).thenReturn(Optional.of(updatedUser));
            when(userMapper.toDto(any(User.class))).thenReturn(outputDto);

            mockMvc.perform(put("/api/auth/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("Oscar Actualizado"));
        }

        @Test
        @DisplayName("Debe retornar 404 Not Found cuando el usuario a actualizar no existe")
        void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
            UserDto inputDto = new UserDto(999L, "Fantasma", "fantasma@correo.com", "Pass123!", true, null, null);
            User domainUser = new User(999L, "Fantasma", "Pass123!", true, "fantasma@correo.com", null, null);

            when(userMapper.toDomain(any(UserDto.class))).thenReturn(domainUser);
            when(userInPort.updateUser(any(User.class))).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/auth/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("No se pudo actualizar. El usuario no existe en el sistema."));
        }
    }

    // 💡 NUEVO BLOQUE: Aquí empujamos el GlobalExceptionHandler al 100%
    @Nested
    @DisplayName("Pruebas para el GlobalExceptionHandler")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando se envía un JSON malformado")
        void shouldReturn400WhenJsonIsMalformed() throws Exception {
            String malformedJson = "{\"username\": \"oscar@correo.com\", \"password\": \"Admin123\" "; // Falta cerrar llave

            mockMvc.perform(post("/api/auth/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando hay un Type Mismatch")
        void shouldReturn400WhenTypeMismatchOccurs() throws Exception {
            // Mandamos letras donde se espera un Long
            mockMvc.perform(get("/api/auth/users/letras")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").isString());
        }

        @Test
        @DisplayName("Debe retornar 409 Conflict cuando el usuario ya existe")
        void shouldReturn409WhenUserAlreadyExists() throws Exception {
            // GIVEN
            UserDto inputDto = new UserDto(null, "Oscar", "oscar@correo.com", "Admin123!", true, null, null);

            // 🚨 ESTO ERA LO QUE FALTABA: Configurar los mocks para simular el error
            when(userMapper.toDomain(any(UserDto.class))).thenReturn(new User(null, "Oscar", "Admin123!", true, "oscar@correo.com", null, null));
            when(userInPort.createUser(any(User.class)))
                    .thenThrow(new com.co.oscar.login.domain.exceptions.UserAlreadyExistsException("El correo ya se encuentra registrado."));

            // WHEN & THEN
            mockMvc.perform(post("/api/auth/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isConflict()) // Ahora sí esperará y recibirá el 409
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("El correo ya se encuentra registrado."));
        }

        @Test
        @DisplayName("Debe retornar 500 Internal Server Error cuando ocurre una excepción general")
        void shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {
            when(userInPort.getUser(1L)).thenThrow(new RuntimeException("Error catastrófico en el servidor"));

            mockMvc.perform(get("/api/auth/users/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("An unexpected internal error occurred. Please try again later."));
        }
    }
}