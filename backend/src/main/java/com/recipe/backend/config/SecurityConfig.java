package com.recipe.backend.config;

import com.recipe.backend.Security.CustomUserDetailsService;
import com.recipe.backend.Security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/nutrition/**").permitAll()

                        .requestMatchers("/api/recipes/generate-ai").hasAnyRole("USER", "ADMIN")

                        // Admin-only endpoints (new path)
                        .requestMatchers("/api/recipes/admin/**").hasRole("ADMIN")

                        // User-specific endpoints
                        .requestMatchers("/api/recipes/my").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/recipes/other").hasAnyRole("USER", "ADMIN")

                                // Diet log endpoints
                                .requestMatchers("/api/dietlog/**").hasAnyRole("USER", "ADMIN")

                               // Profile endpoints
                                .requestMatchers("/api/profile/**").hasAnyRole("USER", "ADMIN")


                                // Other user endpoints
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
