package com.co.oscar.login.domain;


import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import com.co.oscar.login.domain.exceptions.UserException;
import com.co.oscar.login.domain.exceptions.UserNotFoundException;

import java.time.LocalDateTime;


public record User(Long id, String name, String password, boolean isActive, String username, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {

    private static final String SECURITY_PATTERN = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]).{8,16}$";


    public User {

        if (username == null || username.isBlank()) {
            throw new UserNotFoundException(" Required field");
        }
        if (password == null || password.isBlank()) {
            throw new UserException("the password is required.");
        }

    }

    public User encrypt(EncryptedServicePort encryptedServicePort) {
        if (!this.password.matches(SECURITY_PATTERN)) {
            throw new UserException("the password must have at least 8 characters. and at least one special character");
        }
        String encryptedPassword = encryptedServicePort.encode(this.password);
        return new User(
                this.id,
                this.name,
                encryptedPassword,
                this.isActive,
                this.username,
                this.createdAt,
                this.updatedAt
        );
    }

}
