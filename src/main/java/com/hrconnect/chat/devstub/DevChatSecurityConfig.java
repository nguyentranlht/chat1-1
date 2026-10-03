package com.hrconnect.chat.devstub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * BAN TAM: chi bat khi chay profile chat-dev. Mo het API de test bang Postman va trang chat-test.html.
 * Khi ghep: xoa file nay. Trong SecurityFilterChain cua Core, cho phep "/ws/**" (xac thuc WebSocket nam o
 * ChatConnectAuthenticator) va yeu cau dang nhap voi "/api/v1/chats/**".
 */
@Configuration
@Profile("chat-dev")
public class DevChatSecurityConfig {

    @Bean
    public SecurityFilterChain chatDevSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
