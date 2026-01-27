package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.ChatCompletionRequest;
import com.example.publicordercasemanagementsystem.dto.ChatCompletionResponse;
import com.example.publicordercasemanagementsystem.dto.ChatMessage;
import com.example.publicordercasemanagementsystem.dto.PromptRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashScopeService {

    private final WebClient webClient;

    public DashScopeService(@Qualifier("dashScopeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public ChatCompletionResponse chat(ChatCompletionRequest request) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();
    }

    public ChatCompletionResponse chat(PromptRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", request.getPrompt()));

        ChatCompletionRequest chatRequest = new ChatCompletionRequest();
        if (request.getModel() != null && !request.getModel().isBlank()) {
            chatRequest.setModel(request.getModel());
        }
        chatRequest.setMessages(messages);

        return chat(chatRequest);
    }

    public Mono<ChatCompletionResponse> chatAsync(ChatCompletionRequest request) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class);
    }
}
