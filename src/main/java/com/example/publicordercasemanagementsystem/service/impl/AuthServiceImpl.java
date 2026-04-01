package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.AuthResponse;
import com.example.publicordercasemanagementsystem.dto.LoginRequest;
import com.example.publicordercasemanagementsystem.dto.LogoutRequest;
import com.example.publicordercasemanagementsystem.dto.RefreshRequest;
import com.example.publicordercasemanagementsystem.dto.RegisterRequest;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.LoginLogMapper;
import com.example.publicordercasemanagementsystem.mapper.RefreshTokenMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.LoginLog;
import com.example.publicordercasemanagementsystem.pojo.RefreshToken;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.AuthService;
import com.example.publicordercasemanagementsystem.util.CryptoUtil;
import com.example.publicordercasemanagementsystem.util.JwtService;
import com.example.publicordercasemanagementsystem.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserMapper userMapper,
                           RefreshTokenMapper refreshTokenMapper,
                           LoginLogMapper loginLogMapper,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.loginLogMapper = loginLogMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userMapper.findByName(request.getName());
        if (user == null) {
            recordLogin(null, request.getName(), httpRequest, 0);
            throw new AuthException(401, "Invalid name or password");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            recordLogin(user.getId(), user.getName(), httpRequest, 0);
            throw new AuthException(403, "User account is locked or disabled");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            recordLogin(user.getId(), user.getName(), httpRequest, 0);
            throw new AuthException(403, "User account is locked or disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userMapper.incrementLoginAttempts(user.getId());
            int attempts = (user.getLoginAttempts() == null ? 0 : user.getLoginAttempts()) + 1;
            recordLogin(user.getId(), user.getName(), httpRequest, 0);
            if (attempts >= MAX_ATTEMPTS) {
                userMapper.lockUser(user.getId());
                throw new AuthException(429, "Too many login attempts, please try again later",
                        new RetryAfter(LOCK_MINUTES * 60));
            }
            throw new AuthException(401, "Invalid name or password");
        }

        userMapper.updateLoginSuccess(user.getId());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user.getId(), refreshToken);
        recordLogin(user.getId(), user.getName(), httpRequest, 1);

        AuthResponse response = new AuthResponse();
        response.setToken(accessToken);
        response.setExpiresIn(jwtService.getExpirationSeconds());
        response.setRefreshToken(refreshToken);
        response.setUser(toUserInfo(user));
        return response;
    }

    @Override
    public AuthResponse refresh(RefreshRequest request, String authorizationHeader) {
        String accessToken = extractBearerToken(authorizationHeader);
        if (accessToken == null || !jwtService.isTokenValid(accessToken)) {
            throw new AuthException(401, "Invalid or expired token");
        }
        String tokenHash = CryptoUtil.sha256Hex(request.getRefreshToken());
        RefreshToken stored = refreshTokenMapper.findValidByHash(tokenHash);
        if (stored == null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(401, "Invalid or expired token");
        }
        refreshTokenMapper.revokeByHash(tokenHash);
        refreshTokenMapper.updateLastUsed(stored.getId());

        User user = userMapper.findById(stored.getUserId());
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthException(403, "User account is locked or disabled");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user.getId(), newRefreshToken);

        AuthResponse response = new AuthResponse();
        response.setToken(newAccessToken);
        response.setExpiresIn(jwtService.getExpirationSeconds());
        response.setRefreshToken(newRefreshToken);
        response.setUser(toUserInfo(user));
        return response;
    }

    @Override
    public void logout(LogoutRequest request, String authorizationHeader) {
        String accessToken = extractBearerToken(authorizationHeader);
        if (accessToken == null || !jwtService.isTokenValid(accessToken)) {
            throw new AuthException(401, "Invalid token");
        }
        if (request != null && request.getRefreshToken() != null) {
            String tokenHash = CryptoUtil.sha256Hex(request.getRefreshToken());
            refreshTokenMapper.revokeByHash(tokenHash);
        }
        Long userId = jwtService.getUserId(accessToken);
        refreshTokenMapper.revokeByUserId(userId);
    }

    @Override
    public UserInfo register(RegisterRequest request) {
        if (request.getPassword() == null || request.getConfirmPassword() == null
                || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(400, "Passwords do not match");
        }
        User existing = userMapper.findByName(request.getName());
        if (existing != null) {
            throw new AuthException(400, "Name already exists");
        }
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(request.getRole() == null || request.getRole().isBlank() ? "police_officer" : request.getRole());
        user.setDepartmentId(request.getDepartmentId());
        user.setIsActive(true);
        user.setLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        User stored = userMapper.findById(user.getId());
        return stored == null ? toUserInfo(user) : toUserInfo(stored);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(CryptoUtil.sha256Hex(refreshToken));
        token.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpirationSeconds()));
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(token);
    }

    private void recordLogin(Long userId, String name, HttpServletRequest request, int result) {
        if (request == null) {
            return;
        }
        String ip = RequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        RequestUtil.UserAgentInfo agentInfo = RequestUtil.parseUserAgent(userAgent);
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setName(name);
        log.setIp(ip);
        log.setLoginTime(LocalDateTime.now());
        log.setLoginResult(result);
        log.setDeviceType(agentInfo.deviceType());
        log.setBrowser(agentInfo.browser());
        log.setOs(agentInfo.os());
        loginLogMapper.insert(log);
    }

    private UserInfo toUserInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setName(user.getName());
        info.setRole(user.getRole());
        info.setRoleName(user.getRoleName());
        info.setDepartment(user.getDepartment());
        info.setDepartmentId(user.getDepartmentId());
        return info;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private record RetryAfter(int retryAfter) {
    }
}
