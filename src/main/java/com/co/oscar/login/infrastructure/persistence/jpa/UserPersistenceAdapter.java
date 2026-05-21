package com.co.oscar.login.infrastructure.persistence.jpa;

import com.co.oscar.login.application.ports.output.UserOutPort;
import com.co.oscar.login.domain.User;
import com.co.oscar.login.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de persistencia para usuarios usando JPA.
 * Implementa el puerto de salida para operaciones de base de datos.
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserOutPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByUsername(String email) {
        Optional<UserEntity> userEntity = userJpaRepository.findByEmail(email);
        return userEntity.map(userMapper::toDomain);
    }

    @Override
    public Optional<User> createUser(User user) {
        UserEntity savedEntity = userJpaRepository.save(userMapper.toEntity(user));
        return Optional.of(userMapper.toDomain(savedEntity));
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain).filter(User::isActive);
    }

    @Override
    public Optional<User> updateUser(User user) {
        UserEntity savedEntity = userJpaRepository.save(userMapper.toEntity(user));
        return Optional.of(userMapper.toDomain(savedEntity));
    }

    @Override
    public void deleteUser(Long id) {
        userJpaRepository.deleteById(id);
    }
}
