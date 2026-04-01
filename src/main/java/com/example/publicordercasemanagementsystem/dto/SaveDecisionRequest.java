package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class SaveDecisionRequest {

    @NotBlank(message = "decisionResult is required")
    private String decisionResult;

    @NotBlank(message = "decisionContent is required")
    private String decisionContent;

    private String coerciveMeasureCode;

    private LocalDateTime decidedAt;

    public String getDecisionResult() {
        return decisionResult;
    }

    public void setDecisionResult(String decisionResult) {
        this.decisionResult = decisionResult;
    }

    public String getDecisionContent() {
        return decisionContent;
    }

    public void setDecisionContent(String decisionContent) {
        this.decisionContent = decisionContent;
    }

    public String getCoerciveMeasureCode() {
        return coerciveMeasureCode;
    }

    public void setCoerciveMeasureCode(String coerciveMeasureCode) {
        this.coerciveMeasureCode = coerciveMeasureCode;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}

