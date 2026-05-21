package com.co.oscar.login.application.usescases;

import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.application.ports.output.UserOutPort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.domain.exceptions.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Inicializa los mocks de Mockito automáticamente
@DisplayName("Pruebas Unitarias para UserUseCaseImp")
class UserUseCaseImpTest {

    @Mock
    private UserOutPort userOutPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @Mock
    private EncryptedServicePort encryptedServicePort;

    @InjectMocks
    private UserUseCaseImp userUseCase; // Inyecta los 3 mocks de arriba automáticamente en la implementación

    @Nested
    @DisplayName("Pruebas para el método findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("Debe retornar el usuario envuelto en Optional cuando el correo existe")
        void shouldReturnUserWhenEmailExists() {
            // GIVEN
            String email = "oscar@correo.com";
            User mockUser = new User(1L, "Oscar", "hashed_pass", true, email, LocalDateTime.now(), LocalDateTime.now());
            when(userOutPort.findByUsername(email)).thenReturn(Optional.of(mockUser));

            // WHEN
            Optional<User> result = userUseCase.findByEmail(email);

            // THEN
            assertTrue(result.isPresent());
            assertEquals(email, result.get().username());
            verify(userOutPort, times(1)).findByUsername(email);
        }
    }

    @Nested
    @DisplayName("Pruebas para el método login")
    class LoginTests {

        @Test
        @DisplayName("Debe retornar el Token exitosamente si las credenciales son correctas y el usuario está ACTIVO (USER)")
        void shouldReturnTokenWhenCredentialsAreCorrectAndUserIsActive() {
            // GIVEN
            String email = "oscar@correo.com";
            String pass = "Admin123!";
            User mockUser = new User(1L, "Oscar", "hashed_pass", true, email, LocalDateTime.now(), LocalDateTime.now());
            String expectedToken = "jwt.token.dummy";

            when(userOutPort.findByUsername(email)).thenReturn(Optional.of(mockUser));
            when(encryptedServicePort.matches(pass, "hashed_pass")).thenReturn(true);
            when(tokenServicePort.generateToken(eq(email), any(Map.class))).thenReturn(expectedToken);

            // WHEN
            Optional<String> token = userUseCase.login(email, pass);

            // THEN
            assertTrue(token.isPresent());
            assertEquals(expectedToken, token.get());
        }

        @Test
        @DisplayName("Debe asignar el rol GUEST si las credenciales son correctas pero el usuario está INACTIVO")
        void shouldReturnTokenWithGuestRoleWhenUserIsInactive() {
            // GIVEN
            String email = "inactivo@correo.com";
            String pass = "Admin123!";
            User mockUser = new User(2L, "Inactivo", "hashed_pass", false, email, LocalDateTime.now(), LocalDateTime.now());
            String expectedToken = "jwt.token.guest";

            when(userOutPort.findByUsername(email)).thenReturn(Optional.of(mockUser));
            when(encryptedServicePort.matches(pass, "hashed_pass")).thenReturn(true);
            when(tokenServicePort.generateToken(eq(email), any(Map.class))).thenReturn(expectedToken);

            // WHEN
            Optional<String> token = userUseCase.login(email, pass);

            // THEN
            assertTrue(token.isPresent());
            assertEquals(expectedToken, token.get());
        }

        @Test
        @DisplayName("Debe retornar Optional vacío si la contraseña es incorrecta")
        void shouldReturnEmptyOptionalWhenPasswordDoesNotMatch() {
            // GIVEN
            String email = "oscar@correo.com";
            String wrongPass = "WrongPass";
            User mockUser = new User(1L, "Oscar", "hashed_pass", true, email, LocalDateTime.now(), LocalDateTime.now());

            when(userOutPort.findByUsername(email)).thenReturn(Optional.of(mockUser));
            when(encryptedServicePort.matches(wrongPass, "hashed_pass")).thenReturn(false);

            // WHEN
            Optional<String> token = userUseCase.login(email, wrongPass);

            // THEN
            assertTrue(token.isEmpty());
            verify(tokenServicePort, never()).generateToken(anyString(), any(Map.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para el método createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Debe registrar un nuevo usuario encriptando la contraseña si el correo no está duplicado")
        void shouldCreateUserSuccessfully() {
            // GIVEN
            User inputUser = new User(null, "Oscar", "Admin123!", true, "oscar@correo.com", null, null);
            User encryptedUser = new User(null, "Oscar", "hashed_pass", true, "oscar@correo.com", null, null);
            User savedUser = new User(1L, "Oscar", "hashed_pass", true, "oscar@correo.com", LocalDateTime.now(), LocalDateTime.now());

            when(userOutPort.findByUsername("oscar@correo.com")).thenReturn(Optional.empty());
            when(encryptedServicePort.encode(anyString())).thenReturn("hashed_pass");
            when(userOutPort.createUser(any(User.class))).thenReturn(Optional.of(savedUser));

            // WHEN
            Optional<User> result = userUseCase.createUser(inputUser);

            // THEN
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().id());
            assertEquals("hashed_pass", result.get().password());
        }

        @Test
        @DisplayName("Debe lanzar una UserException si el correo del usuario ya existe en el sistema")
        void shouldThrowExceptionWhenUserEmailAlreadyExists() {
            // GIVEN
            User inputUser = new User(null, "Oscar", "Admin123!", true, "oscar@correo.com", null, null);
            User existingUser = new User(1L, "Oscar Viejo", "hashed_pass", true, "oscar@correo.com", LocalDateTime.now(), LocalDateTime.now());

            when(userOutPort.findByUsername("oscar@correo.com")).thenReturn(Optional.of(existingUser));

            // WHEN & THEN
            UserException exception = assertThrows(UserException.class, () -> userUseCase.createUser(inputUser));
            assertEquals("El correo 'oscar@correo.com' ya se encuentra registrado.", exception.getMessage());
            verify(userOutPort, never()).createUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para el método getUser")
    class GetUserTests {

        @Test
        @DisplayName("Debe retornar el usuario correspondiente al ID solicitado")
        void shouldReturnUserById() {
            // GIVEN
            Long id = 1L;
            User mockUser = new User(id, "Oscar", "hashed_pass", true, "oscar@correo.com", LocalDateTime.now(), LocalDateTime.now());
            when(userOutPort.getUser(id)).thenReturn(Optional.of(mockUser));

            // WHEN
            Optional<User> result = userUseCase.getUser(id);

            // THEN
            assertTrue(result.isPresent());
            assertEquals(id, result.get().id());
        }
    }

    @Nested
    @DisplayName("Pruebas para el método updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("Debe actualizar con éxito respetando propiedades inmutables (ID, username, createdAt)")
        void shouldUpdateUserSuccessfullyWhenUserExists() {
            // GIVEN
            LocalDateTime dateOriginal = LocalDateTime.now().minusDays(1);
            LocalDateTime dateUpdate = LocalDateTime.now();

            User existingUser = new User(1L, "Oscar Viejo", "old_pass", true, "oscar@correo.com", dateOriginal, null);
            User updatedInfo = new User(null, "Oscar Nuevo", "new_pass", true, "oscar@correo.com", null, dateUpdate);
            User savedUser = new User(1L, "Oscar Nuevo", "new_pass", true, "oscar@correo.com", dateOriginal, dateUpdate);

            when(userOutPort.findByUsername("oscar@correo.com")).thenReturn(Optional.of(existingUser));
            when(userOutPort.updateUser(any(User.class))).thenReturn(Optional.of(savedUser));

            // WHEN
            Optional<User> result = userUseCase.updateUser(updatedInfo);

            // THEN
            assertTrue(result.isPresent());
            assertEquals("Oscar Nuevo", result.get().name());
            assertEquals(1L, result.get().id()); // Mantiene el ID existente
            assertEquals(dateOriginal, result.get().createdAt()); // Mantiene la fecha de creación original
        }

        @Test
        @DisplayName("Debe retornar un Optional vacío si el usuario que se quiere actualizar no existe")
        void shouldReturnEmptyOptionalWhenUserToUpdateDoesNotExist() {
            // GIVEN
            User updatedInfo = new User(null, "Fantasma", "pass", true, "fantasma@correo.com", null, LocalDateTime.now());
            when(userOutPort.findByUsername("fantasma@correo.com")).thenReturn(Optional.empty());

            // WHEN
            Optional<User> result = userUseCase.updateUser(updatedInfo);

            // THEN
            assertTrue(result.isEmpty());
            verify(userOutPort, never()).updateUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para el método deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("Debe invocar la eliminación del usuario en el puerto de salida")
        void shouldInvokeDeleteInOutputPort() {
            // GIVEN
            Long id = 1L;
            doNothing().when(userOutPort).deleteUser(id);

            // WHEN
            userUseCase.deleteUser(id);

            // THEN
            verify(userOutPort, times(1)).deleteUser(id);
        }
    }
}