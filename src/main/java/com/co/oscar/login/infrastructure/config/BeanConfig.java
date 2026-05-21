package com.co.oscar.login.infrastructure.config;

import com.co.oscar.login.application.ports.input.UserInPort;
import com.co.oscar.login.application.ports.output.EncryptedServicePort;
import com.co.oscar.login.application.ports.output.TokenServicePort;
import com.co.oscar.login.application.ports.output.UserOutPort;
import com.co.oscar.login.application.usescases.UserUseCaseImp;
import com.co.oscar.login.infrastructure.security.RefreshTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UserInPort userInPort(UserOutPort userOutPort, TokenServicePort tokenServicePort,
                                 EncryptedServicePort hashServicePort, RefreshTokenService refreshTokenService) {
        return new UserUseCaseImp(userOutPort, tokenServicePort, hashServicePort, refreshTokenService);
    }

}
