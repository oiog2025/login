package com.co.oscar.login.application.usescases;

import com.co.oscar.login.application.ports.input.UserInPort;
import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.application.ports.output.UserOutPort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.domain.exceptions.UserAlreadyExistsException;
import com.co.oscar.login.infrastructure.entrypoints.dtos.login.LoginResponseDTO;
import com.co.oscar.login.infrastructure.security.RefreshTokenService;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del caso de uso para operaciones de usuario. Contiene la lógica de negocio para
 * autenticación y búsqueda de usuarios.
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
    return userOutPort
        .findByUsername(email)
        .filter(User::isActive)
        .filter(user -> encryptedServicePort.matches(password, user.password()))
        .map(
            user -> {
              Map<String, Object> claims = new HashMap<>();
              claims.put("id", user.id());
              claims.put("isActive", user.isActive());
              claims.put("role", "USER");
              claims.put("user", user.email());
              return tokenServicePort.generateToken(user.email(), claims);
            });
  }

  @Override
  public Optional<LoginResponseDTO> loginWithRefreshToken(String email, String password) {
    return userOutPort
        .findByUsername(email)
        .filter(User::isActive)
        .filter(user -> encryptedServicePort.matches(password, user.password()))
        .map(
            user -> {
              Map<String, Object> claims = new HashMap<>();
              claims.put("id", user.id());
              claims.put("isActive", user.isActive());
              claims.put("role", "USER");
              claims.put("user", user.email());

              String accessToken = tokenServicePort.generateToken(user.email(), claims);
              String refreshToken = refreshTokenService.createRefreshToken(user.email());

              return LoginResponseDTO.builder()
                  .accessToken(accessToken)
                  .refreshToken(refreshToken)
                  .build();
            });
  }

  @Override
  public Optional<User> createUser(User user) {

    if (userOutPort.findByUsername(user.email()).isPresent()) {
      throw new UserAlreadyExistsException(
          "El correo '" + user.email() + "' ya se encuentra registrado.");
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
    return userOutPort
        .findByUsername(user.email())
        .flatMap(
            existingUser -> {
              boolean hasNewPassword = user.password() != null && !user.password().isBlank();

              String passwordToSave = hasNewPassword ? user.password() : existingUser.password();
              User updatedUser =
                  new User(
                      existingUser.id(),
                      user.name(),
                      passwordToSave,
                      user.isActive(),
                      existingUser.email(),
                      existingUser.createdAt(),
                      user.updatedAt());
              if (hasNewPassword) {
                updatedUser = updatedUser.encrypt(encryptedServicePort);
              }
              return userOutPort.updateUser(updatedUser);
            });
  }

  @Override
  public void deleteUser(Long id) {
    userOutPort.deleteUser(id);
  }

  @Override
  public Optional<List<User>> getAllUser() {
    return userOutPort.getAllUser();
  }
}
