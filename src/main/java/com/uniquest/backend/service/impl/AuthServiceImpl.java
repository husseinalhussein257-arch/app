package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.auth.LoginRequest;
import com.uniquest.backend.dto.auth.LoginResponse;
import com.uniquest.backend.dto.auth.RegistrationRequest;
import com.uniquest.backend.enums.Role;
import com.uniquest.backend.exception.ConflictException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.User;
import com.uniquest.backend.repository.BranchRepository;
import com.uniquest.backend.repository.UniversityRepository;
import com.uniquest.backend.repository.UserRepository;
import com.uniquest.backend.security.JwtService;
import com.uniquest.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login flow:
 *   1. AuthenticationManager.authenticate() delegates to DaoAuthenticationProvider
 *   2. DaoAuthenticationProvider calls CustomUserDetailsService.loadUserByUsername()
 *   3. BCrypt password comparison happens inside the provider
 *   4. If invalid, BadCredentialsException is thrown and caught by GlobalExceptionHandler
 *   5. If valid, we load the User for role info and issue a JWT
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final BranchRepository branchRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Step 1: authenticate — throws BadCredentialsException on failure
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Step 2: load User to get role and universityId (authentication succeeded, so user exists)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();  // unreachable after successful authentication

        // Step 3: issue JWT with username + role + universityId embedded
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getUniversityId()
        );

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole())
                .build();
    }

    @Override
    public LoginResponse register(RegistrationRequest request) {
        // Step 1: validate username is not taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }

        // Step 2: validate university exists
        if (!universityRepository.existsByIdAndDeletedFalse(request.getUniversityId())) {
            throw new ResourceNotFoundException("University", request.getUniversityId());
        }

        // Step 3: validate branch exists
        if (!branchRepository.existsByIdAndDeletedFalse(request.getBranchId())) {
            throw new ResourceNotFoundException("Branch", request.getBranchId());
        }

        // Step 4: create user with hashed password and assigned university/branch
        // Role is always STUDENT for public registration — admin accounts are created separately
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .universityId(request.getUniversityId())
                .branchId(request.getBranchId())
                .build();

        User savedUser = userRepository.save(user);

        // Step 5: issue JWT with all claims
        String token = jwtService.generateToken(
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getUniversityId()
        );

        return LoginResponse.builder()
                .token(token)
                .role(savedUser.getRole())
                .build();
    }
}
