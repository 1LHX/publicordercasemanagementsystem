package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.AuthResponse;
import com.example.publicordercasemanagementsystem.dto.LoginRequest;
import com.example.publicordercasemanagementsystem.dto.LogoutRequest;
import com.example.publicordercasemanagementsystem.dto.RefreshRequest;
import com.example.publicordercasemanagementsystem.dto.RegisterRequest;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refresh(RefreshRequest request, String authorizationHeader);

    void logout(LogoutRequest request, String authorizationHeader);

    UserInfo register(RegisterRequest request);
}
