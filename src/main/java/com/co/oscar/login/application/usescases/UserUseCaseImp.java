package com.co.oscar.login.application.usescases;

import com.co.oscar.login.application.ports.input.UserInPort;
import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.application.ports.output.UserOutPort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.domain.exceptions.UserAlreadyExistsException;
import com.co.oscar.login.infrastructure.entrypoints.dtos.LoginResponseDTO;
import com.co.oscar.login.infrastructure.security.RefreshTokenService;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del caso de uso para operaciones de usuario.
 * Contiene la lógica de negocio para autenticación y búsqueda de usuarios.
 */
@AllArgsConstructor
public class UserUseCaseImp implements UserInPort {

    private final UserOutPort userOutPort;
    private final TokenServicePort tokenServicePort;
    private final EncryptedServicePort encryptedServicePort;
    private final RefreshTokenService refreshTokenService;


    @Override
    public Optional<User> findByEmail(String email) {
        return userOutPort.findByUsername(email);
    }

    public Optional<String> login(String email, String password) {
        return userOutPort.findByUsername(email)
                .filter(User::isActive)
                .filter(user -> encryptedServicePort.matches(password, user.password()))
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("id", user.id());
                    claims.put("isActive", user.isActive());
                    claims.put("role", "USER");
                    claims.put("user", user.username());
                    return tokenServicePort.generateToken(user.username(), claims);
                });
    }

    @Override
    public Optional<LoginResponseDTO> loginWithRefreshToken(String email, String password) {
        return userOutPort.findByUsername(email)
                .filter(User::isActive)
                .filter(user -> encryptedServicePort.matches(password, user.password()))
                .map(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("id", user.id());
                    claims.put("isActive", user.isActive());
                    claims.put("role", "USER");
                    claims.put("user", user.username());

                    String accessToken = tokenServicePort.generateToken(user.username(), claims);
                    String refreshToken = refreshTokenService.createRefreshToken(user.username());

                    return LoginResponseDTO.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();
                });
    }

    @Override
    public Optional<User> createUser(User user) {

        if (userOutPort.findByUsername(user.username()).isPresent()) {
            throw new UserAlreadyExistsException("El correo '" + user.username() + "' ya se encuentra registrado.");
        }
        User userWithHashedPassword = user.encrypt(encryptedServicePort);
        return userOutPort.createUser(userWithHashedPassword);
    }

    @Override
    public Optional<User> getUser(Long id) {

        return userOutPort.getUser(id);
    }

    @Override
    public Optional<User> updateUser(User user) {
        return userOutPort.findByUsername(user.username()).flatMap(
                existingUser -> {
                    User updatedUser = new User(
                            existingUser.id(),
                            user.name(),
                            user.password(),
                            user.isActive(),
                            existingUser.username(),
                            existingUser.createdAt(),
                            user.updatedAt()
                    ).encrypt(encryptedServicePort);
                    return userOutPort.updateUser(updatedUser);
                });
    }

    @Override
    public void deleteUser(Long id) {
        userOutPort.deleteUser(id);
    }

}
