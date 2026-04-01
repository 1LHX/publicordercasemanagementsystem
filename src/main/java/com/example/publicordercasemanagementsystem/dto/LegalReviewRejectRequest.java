package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

public class LegalReviewRejectRequest {

    @NotBlank(message = "reason is required")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

