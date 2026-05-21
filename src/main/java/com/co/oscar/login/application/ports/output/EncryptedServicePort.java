package com.co.oscar.login.application.ports.output;

/**
 * Puerto de salida para servicios de hash de contraseñas.
 * Define el contrato para codificar y verificar contraseñas.
 */
public interface EncryptedServicePort {

    /**
     * Codifica una contraseña en texto plano.
     *
     * @param password Contraseña en texto plano
     * @return Contraseña codificada
     */
    String encode(String password);

    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña codificada.
     *
     * @param rawPassword     Contraseña en texto plano
     * @param encodedPassword Contraseña codificada
     * @return true si coinciden, false si no
     */
    boolean matches(String rawPassword, String encodedPassword);
}
