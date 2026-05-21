package com.co.oscar.login.application.ports.output;

import java.util.Map;

/**
 * Puerto de salida para servicios de tokens JWT.
 * Define el contrato para generar, validar y extraer información de tokens.
 */
public interface TokenServicePort {

    /**
     * Genera un token JWT para un usuario.
     * @param username Nombre de usuario
     * @param claims Claims adicionales a incluir en el token
     * @return Token JWT generado
     */
    String generateToken(String username, Map<String, Object> claims);

    /**
     * Extrae el nombre de usuario de un token JWT.
     * @param token Token JWT
     * @return Nombre de usuario extraído del token
     */
    String extractUsername(String token);

    /**
     * Verifica si un token JWT es válido para un usuario específico.
     * @param token Token JWT a validar
     * @param username Nombre de usuario esperado
     * @return true si el token es válido, false si no
     */
    boolean isTokenValid(String token, String username);

    /**
     * Genera un refresh token para un usuario.
     * @param username Nombre de usuario
     * @return Refresh token generado
     */
    String generateRefreshToken(String username);
}
