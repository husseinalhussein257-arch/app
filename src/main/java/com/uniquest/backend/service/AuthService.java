package com.uniquest.backend.service;

import com.uniquest.backend.dto.auth.LoginRequest;
import com.uniquest.backend.dto.auth.LoginResponse;
import com.uniquest.backend.dto.auth.RegistrationRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegistrationRequest request);
}
