package com.co.oscar.login.infrastructure.config;

import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador para servicios de hash usando BCrypt.
 * Implementa el puerto de salida para operaciones de hash de contraseñas.
 */
@Component
@RequiredArgsConstructor
public class BCryptHashAdapter implements EncryptedServicePort {

    private final PasswordEncoder passwordEncoder;

    public String encode(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
