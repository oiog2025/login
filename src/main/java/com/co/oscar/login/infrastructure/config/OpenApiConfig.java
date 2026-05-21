package com.co.oscar.login.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("API de Autenticación - Oscar Login")
                .version("1.0")
                .description("Servicios de dominio para gestión de usuarios y seguridad");

        return new OpenAPI().info(info);
    }
}
