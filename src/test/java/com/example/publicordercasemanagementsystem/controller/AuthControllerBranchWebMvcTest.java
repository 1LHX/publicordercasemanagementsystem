package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.AuthResponse;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerBranchWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void loginShouldReturnBusinessError() throws Exception {
        when(authService.login(any(), any())).thenThrow(new AuthException(401, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"name\":\"alice\"," +
                                "\"password\":\"wrong\"" +
                                "}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void refreshShouldReturnOk() throws Exception {
        when(authService.refresh(any(), anyString())).thenReturn(buildAuthResponse("new-token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer old-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"r1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.token").value("new-token"));
    }

    @Test
    void refreshShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer old-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void refreshShouldReturnBusinessError() throws Exception {
        when(authService.refresh(any(), anyString())).thenThrow(new AuthException(401, "Refresh token invalid"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer old-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"bad\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token invalid"));
    }

    @Test
    void logoutShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"r1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void logoutShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void logoutShouldReturnBusinessError() throws Exception {
        doThrow(new AuthException(401, "Token already revoked"))
                .when(authService).logout(any(), anyString());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"r1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token already revoked"));
    }

    @Test
    void registerShouldReturnOk() throws Exception {
        UserInfo user = new UserInfo();
        user.setId(2L);
        user.setName("bob");
        when(authService.register(any())).thenReturn(user);

        String requestBody = """
                {
                  "name": "bob",
                  "password": "P@ssw0rd",
                  "confirmPassword": "P@ssw0rd"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Register successful"))
                .andExpect(jsonPath("$.data.name").value("bob"));
    }

    @Test
    void registerShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void registerShouldReturnBusinessError() throws Exception {
        when(authService.register(any())).thenThrow(new AuthException(409, "User already exists"));

        String requestBody = """
                {
                  "name": "alice",
                  "password": "P@ssw0rd",
                  "confirmPassword": "P@ssw0rd"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    private AuthResponse buildAuthResponse(String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken("refresh-token");
        response.setExpiresIn(3600);
        return response;
    }
}

