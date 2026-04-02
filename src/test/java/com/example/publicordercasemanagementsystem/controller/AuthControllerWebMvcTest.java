package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.AuthResponse;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerWebMvcTest {

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
    void loginShouldReturnApiResponseEnvelope() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("token-value");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setExpiresIn(3600);

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setName("alice");
        userInfo.setRole("ADMIN");
        authResponse.setUser(userInfo);

        when(authService.login(any(), any())).thenReturn(authResponse);

        String requestBody = """
                {
                  "name": "alice",
                  "password": "P@ssw0rd"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("token-value"))
                .andExpect(jsonPath("$.data.user.name").value("alice"));
    }

    @Test
    void loginShouldReturnValidationFailedWhenNameMissing() throws Exception {
        String requestBody = """
                {
                  "password": "P@ssw0rd"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
