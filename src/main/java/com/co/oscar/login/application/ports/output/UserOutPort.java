package com.co.oscar.login.application.ports.output;

import com.co.oscar.login.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de usuarios.
 * Define el contrato que los adaptadores de persistencia deben implementar.
 */
public interface UserOutPort {

    /**
     * Busca un usuario por su correo electrónico en la base de datos.
     *
     * @param email Correo electrónico del usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByUsername(String email);

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

    Optional<List<User>> getAllUser();
}
