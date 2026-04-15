package com.uniquest.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request, extracts the JWT from the Authorization header,
 * validates it, and injects the authenticated principal into the SecurityContext.
 *
 * If no valid JWT is present the request passes through unauthenticated —
 * Spring Security will then enforce access rules (e.g. reject /admin/**).
 *
 * Extends OncePerRequestFilter to guarantee single execution per request
 * even in async dispatches.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token present — pass through and let Spring Security handle auth rules
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String username = jwtService.extractUsername(token);

            // Only set authentication if not already set (avoid double processing)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Extract claims from token
                String role = jwtService.extractRole(token);
                String universityId = jwtService.extractUniversityId(token);

                // Create CustomUserDetails from JWT claims (no DB query needed)
                CustomUserDetails customUserDetails = new CustomUserDetails(
                        username,
                        "",                    // password not needed post-auth
                        role,
                        universityId
                );

                // Validate token against constructed user details
                if (jwtService.isTokenValid(token, customUserDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    customUserDetails,
                                    null,                          // no credentials needed post-auth
                                    customUserDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid token — log and continue unauthenticated.
            // Spring Security will enforce access rules downstream.
            logger.warn("JWT processing failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
