package com.uniquest.backend.security;

import com.uniquest.backend.model.User;
import com.uniquest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges Spring Security's UserDetailsService with our MongoDB User collection.
 *
 * Spring Security uses the returned UserDetails object to:
 *   1. Verify the password during DaoAuthenticationProvider login
 *   2. Populate the SecurityContext with authorities (ROLE_ADMIN / ROLE_STUDENT)
 *
 * Authorities follow Spring Security's ROLE_ prefix convention so that
 * @PreAuthorize("hasRole('ADMIN')") works correctly.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        // Spring Security requires the authority string to be "ROLE_<ROLE_NAME>"
        // when using hasRole() expressions.
        String authority = "ROLE_" + user.getRole().name(); // e.g. ROLE_ADMIN, ROLE_STUDENT

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())   // BCrypt hash
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
