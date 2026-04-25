package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.AuthResponse;
import com.example.publicordercasemanagementsystem.dto.LogoutRequest;
import com.example.publicordercasemanagementsystem.dto.RefreshRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.LoginLogMapper;
import com.example.publicordercasemanagementsystem.mapper.RefreshTokenMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.RefreshToken;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.util.CryptoUtil;
import com.example.publicordercasemanagementsystem.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplSessionIsolationTest {

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

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, refreshTokenMapper, loginLogMapper, passwordEncoder, jwtService);
    }

    @Test
    void refreshShouldRejectWhenRefreshTokenNotOwnedByAccessUser() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-2");
        String tokenHash = CryptoUtil.sha256Hex("refresh-2");

        when(jwtService.isTokenValid("access-1")).thenReturn(true);
        when(jwtService.getUserId("access-1")).thenReturn(1L);
        when(refreshTokenMapper.findValidByHashAndUserId(tokenHash, 1L)).thenReturn(null);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.refresh(request, "Bearer access-1"));

        assertEquals(401, ex.getStatus());
        verify(refreshTokenMapper, never()).revokeByHash(any());
        verify(refreshTokenMapper, never()).revokeByUserId(any());
    }

    @Test
    void refreshShouldRotateTokenWithinSameUser() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-1");
        String tokenHash = CryptoUtil.sha256Hex("refresh-1");

        RefreshToken stored = new RefreshToken();
        stored.setId(9L);
        stored.setUserId(1L);
        stored.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        User user = new User();
        user.setId(1L);
        user.setIsActive(true);

        when(jwtService.isTokenValid("access-1")).thenReturn(true);
        when(jwtService.getUserId("access-1")).thenReturn(1L);
        when(refreshTokenMapper.findValidByHashAndUserId(tokenHash, 1L)).thenReturn(stored);
        when(userMapper.findById(1L)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.refresh(request, "Bearer access-1");

        assertEquals("new-access", response.getToken());
        assertEquals("new-refresh", response.getRefreshToken());
        verify(refreshTokenMapper).revokeByHashAndUserId(tokenHash, 1L);
        verify(refreshTokenMapper).updateLastUsed(9L);
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
    }

    @Test
    void logoutShouldRevokeAllTokensForCurrentUserAfterOwnershipCheck() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-3");
        String tokenHash = CryptoUtil.sha256Hex("refresh-3");

        when(jwtService.isTokenValid("access-3")).thenReturn(true);
        when(jwtService.getUserId("access-3")).thenReturn(3L);
        when(refreshTokenMapper.revokeByHashAndUserId(tokenHash, 3L)).thenReturn(1);

        authService.logout(request, "Bearer access-3");

        verify(refreshTokenMapper).revokeByHashAndUserId(eq(tokenHash), eq(3L));
        verify(refreshTokenMapper).revokeByUserId(3L);
    }

    @Test
    void logoutShouldRejectRefreshTokenNotOwnedByCurrentUser() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-4");
        String tokenHash = CryptoUtil.sha256Hex("refresh-4");

        when(jwtService.isTokenValid("access-4")).thenReturn(true);
        when(jwtService.getUserId("access-4")).thenReturn(4L);
        when(refreshTokenMapper.revokeByHashAndUserId(tokenHash, 4L)).thenReturn(0);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.logout(request, "Bearer access-4"));

        assertEquals(401, ex.getStatus());
        verify(refreshTokenMapper, never()).revokeByUserId(any());
    }
}

