package com.handmade.config;

import com.handmade.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

       http
    .cors(cors -> {})
    .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS)
            )

        .authorizeHttpRequests(auth -> auth

    .requestMatchers("/api/auth/**")
    .permitAll()

    .requestMatchers(
        HttpMethod.GET,
        "/api/products",
        "/api/products/**"
    )
    .permitAll()

    .requestMatchers(
        HttpMethod.GET,
        "/api/payments/**"
    )
    .authenticated()

    .requestMatchers(
        HttpMethod.POST,
        "/api/payments/**"
    )
    .authenticated()

    .requestMatchers(
        HttpMethod.PUT,
        "/api/orders/**"
    )
    .authenticated()

    .anyRequest()
    .authenticated()
)

.exceptionHandling(exception -> exception
    .accessDeniedHandler((request, response, ex) -> {

        System.out.println("========== ACCESS DENIED ==========");
        System.out.println("METHOD: " + request.getMethod());
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("AUTH: " +
            org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication());
        System.out.println("AUTHORITIES: " +
            org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities());
        System.out.println("REASON: " + ex.getMessage());
        System.out.println("===================================");

        response.sendError(403, "Access denied");
    })
)

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}