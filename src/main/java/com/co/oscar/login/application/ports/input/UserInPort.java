package com.co.oscar.login.application.ports.input;

import com.co.oscar.login.domain.User;
import com.co.oscar.login.infrastructure.entrypoints.dtos.LoginResponseDTO;

import java.util.Optional;

/**
 * Puerto de entrada para operaciones relacionadas con usuarios.
 * Define los contratos que la capa de aplicación expone a los adaptadores de entrada.
 */
public interface UserInPort {
    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email Correo electrónico del usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Autentica un usuario con sus credenciales.
     *
     * @param email    Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return Optional con el token JWT si la autenticación es exitosa, vacío si falla
     */
    Optional<String> login(String email, String password);

    /**
     * Autentica un usuario con sus credenciales y retorna ambos tokens.
     *
     * @param email    Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return Optional con LoginResponseDTO conteniendo access token y refresh token
     */
    Optional<LoginResponseDTO> loginWithRefreshToken(String email, String password);

    /**
     * Crea un nuevo usuario.
     *
     * @param user Usuario a crear
     * @return Optional con el usuario creado si es exitoso, vacío si falla
     */
    Optional<User> createUser(User user);

    /**
     * Busca un usuario por su ID.
     *
     * @param id id del usuario
     * @return Optional con el usuario encontrado si es exitoso, vacío si falla
     */
    Optional<User> getUser(Long id);

    /**
     * Actualiza un usuario.
     *
     * @param user Usuario a actualizar
     * @return Optional con el usuario actualizar si es exitoso, vacío si falla
     */
    Optional<User> updateUser(User user);

    /**
     * Elimina un usuario.
     *
     * @param id id del usuario
     */
    void deleteUser(Long id);
}
