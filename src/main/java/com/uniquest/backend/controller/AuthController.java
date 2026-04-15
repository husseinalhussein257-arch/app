package com.uniquest.backend.controller;

import com.uniquest.backend.dto.auth.LoginRequest;
import com.uniquest.backend.dto.auth.LoginResponse;
import com.uniquest.backend.dto.auth.RegistrationRequest;
import com.uniquest.backend.dto.common.ApiResponse;
import com.uniquest.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     *
     * Public endpoint — no JWT required.
     *
     * Request:
     *   { "username": "alice", "password": "secret123" }
     *
     * Response 200:
     *   { "success": true, "data": { "token": "<jwt>", "role": "STUDENT" } }
     *
     * Response 401:
     *   { "success": false, "message": "Invalid credentials" }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * POST /auth/register
     *
     * Public endpoint for student self-registration — no JWT required.
     * Creates a new STUDENT user with university and branch assignment.
     * Admin accounts are created separately by administrators.
     *
     * Request:
     *   {
     *     "username": "john",
     *     "password": "secret123",
     *     "universityId": "...",
     *     "branchId": "..."
     *   }
     *
     * Response 201:
     *   { "success": true, "data": { "token": "<jwt>", "role": "STUDENT" } }
     *
     * Response 409: username already taken
     * Response 404: university or branch not found
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegistrationRequest request) {

        LoginResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(response));
    }
}
