package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.ChatCompletionRequest;
import com.example.publicordercasemanagementsystem.dto.ChatCompletionResponse;
import com.example.publicordercasemanagementsystem.dto.PromptRequest;
import com.example.publicordercasemanagementsystem.service.DashScopeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.publicordercasemanagementsystem.dto.CaseDocumentRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDocumentPlainRequest;

@RestController
@RequestMapping("/api/dashscope")
public class DashScopeController {

    private final DashScopeService dashScopeService;

    public DashScopeController(DashScopeService dashScopeService) {
        this.dashScopeService = dashScopeService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatCompletionResponse>> chat(@Valid @RequestBody ChatCompletionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dashScopeService.chat(request)));
    }

    @PostMapping("/prompt")
    public ResponseEntity<ApiResponse<ChatCompletionResponse>> chatFromPrompt(@Valid @RequestBody PromptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dashScopeService.chat(request)));
    }

    @PostMapping("/generate-case-document")
    public ResponseEntity<ApiResponse<ChatCompletionResponse>> generateCaseDocument(@Valid @RequestBody CaseDocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dashScopeService.generateCaseDocument(request), "Case document generated successfully"));
    }

    @PostMapping("/generate-case-document-plain")
    public ResponseEntity<ApiResponse<String>> generateCaseDocumentPlain(@Valid @RequestBody CaseDocumentPlainRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(dashScopeService.generateCaseDocumentPlain(request), "Case document generated successfully"));
    }
}
