package com.co.oscar.login.application.ports.output;

public interface RefreshTokenOutPort {
    
    String createRefreshToken(String username);

    void revokeToken(String token);

    boolean isTokenValid(String token);

    String getUsernameFromToken(String token);
}