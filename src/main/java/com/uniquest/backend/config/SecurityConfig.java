package com.uniquest.backend.config;

import com.uniquest.backend.security.CustomUserDetailsService;
import com.uniquest.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 6 configuration.
 *
 * Key decisions:
 *   - Stateless sessions: no HttpSession, no JSESSIONID cookie — JWT only.
 *   - CSRF disabled: not needed for stateless REST APIs consumed by an SPA.
 *   - @EnableMethodSecurity: enables @PreAuthorize on controllers.
 *   - URL rules are the coarse-grained layer; @PreAuthorize is the fine-grained layer.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize / @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CORS — must come before CSRF disable ───────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Disable CSRF (stateless REST API) ──────────────────────────
            .csrf(AbstractHttpConfigurer::disable)

            // ── Stateless session management ───────────────────────────────
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── URL-level access rules ──────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
                    .requestMatchers("/auth/**").permitAll()        // login — public
                    .requestMatchers("/universities", "/branches").permitAll() // discovery — public
                    .requestMatchers("/admin/**").hasRole("ADMIN")  // admin panel
                    .requestMatchers("/student/**").hasRole("STUDENT") // quiz flow
                    .anyRequest().authenticated()                   // /years, /subjects require auth
            )

            // ── Wire in our JWT filter + DaoAuthenticationProvider ─────────
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * DaoAuthenticationProvider uses our UserDetailsService and BCrypt encoder.
     * This is what Spring Security calls during AuthenticationManager.authenticate().
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt with default strength (10 rounds) — suitable for production.
     * Exposed as a bean so services can inject it for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean — used by AuthServiceImpl to authenticate
     * username/password during login without manually calling the provider.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS policy — permits the Angular dev server to call any endpoint.
     *
     * In production, replace the allowed origin with the actual domain.
     * allowCredentials(true) is required for the browser to forward the
     * Authorization header on cross-origin requests.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
