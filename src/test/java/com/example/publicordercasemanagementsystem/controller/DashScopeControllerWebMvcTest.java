package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ChatCompletionResponse;
import com.example.publicordercasemanagementsystem.dto.ChatCompletionRequest;
import com.example.publicordercasemanagementsystem.dto.PromptRequest;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.service.DashScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashScopeControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private DashScopeService dashScopeService;

    @InjectMocks
    private DashScopeController dashScopeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashScopeController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void chatShouldReturnOk() throws Exception {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId("resp-1");
        when(dashScopeService.chat(any(ChatCompletionRequest.class))).thenReturn(response);

        String requestBody = """
                {
                  "model": "qwen-plus",
                  "messages": [
                    {"role": "user", "content": "hello"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/dashscope/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("resp-1"));
    }

    @Test
    void chatShouldReturnBusinessErrorFromUpstream() throws Exception {
        when(dashScopeService.chat(any(ChatCompletionRequest.class))).thenThrow(WebClientResponseException.create(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                HttpHeaders.EMPTY,
                "upstream timeout".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        ));

        String requestBody = """
                {
                  "model": "qwen-plus",
                  "messages": [
                    {"role": "user", "content": "hello"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/dashscope/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502))
                .andExpect(jsonPath("$.message").value("upstream timeout"));
    }

    @Test
    void promptShouldReturnOk() throws Exception {
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId("resp-2");
        when(dashScopeService.chat(any(PromptRequest.class))).thenReturn(response);

        String requestBody = """
                {
                  "prompt": "Summarize this case"
                }
                """;

        mockMvc.perform(post("/api/dashscope/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("resp-2"));
    }

    @Test
    void promptShouldReturnValidationFailedWhenPromptMissing() throws Exception {
        mockMvc.perform(post("/api/dashscope/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void promptShouldReturnBusinessErrorFromUpstream() throws Exception {
        when(dashScopeService.chat(any(PromptRequest.class))).thenThrow(WebClientResponseException.create(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                HttpHeaders.EMPTY,
                "rate limited".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        ));

        String requestBody = """
                {
                  "prompt": "Summarize this case"
                }
                """;

        mockMvc.perform(post("/api/dashscope/prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("rate limited"));
    }
}

