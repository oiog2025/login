package com.co.oscar.login.infrastructure.persistence.jpa.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String username);
}
