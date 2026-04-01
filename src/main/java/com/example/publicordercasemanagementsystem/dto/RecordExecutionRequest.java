package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class RecordExecutionRequest {

    @NotBlank(message = "executionResult is required")
    private String executionResult;

    private String executionNote;

    private LocalDateTime executedAt;

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public String getExecutionNote() {
        return executionNote;
    }

    public void setExecutionNote(String executionNote) {
        this.executionNote = executionNote;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}

