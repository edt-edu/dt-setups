package org.example.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MongoSecurityConfig {

    @Bean(name = "mongoFilterChain")
    @Order(2)  // This ensures it runs after the main security config
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/mongodb/**")  // Only handle MongoDB endpoints
            .csrf((csrf) -> csrf.disable())
            .authorizeHttpRequests((auth) -> auth
                .anyRequest().permitAll())
            .build();
    }
}
