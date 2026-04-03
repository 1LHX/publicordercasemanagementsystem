package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.RegisterRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.LoginLogMapper;
import com.example.publicordercasemanagementsystem.mapper.RefreshTokenMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.service.AuthService;
import com.example.publicordercasemanagementsystem.util.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplRegisterTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private LoginLogMapper loginLogMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @Test
    void registerShouldRejectUnknownRoleCode() {
        AuthService authService = new AuthServiceImpl(
                userMapper,
                refreshTokenMapper,
                loginLogMapper,
                passwordEncoder,
                jwtService
        );

        RegisterRequest request = new RegisterRequest();
        request.setName("new-user");
        request.setPassword("P@ssw0rd");
        request.setConfirmPassword("P@ssw0rd");
        request.setRole("OFFICER");

        when(userMapper.findByName("new-user")).thenReturn(null);
        when(userMapper.countActiveRoleByCode("OFFICER")).thenReturn(0L);

        AuthException ex = assertThrows(AuthException.class, () -> authService.register(request));

        assertEquals(400, ex.getStatus());
        assertTrue(ex.getMessage().contains("Invalid role code"));
        verify(userMapper, never()).insert(any());
    }
}

