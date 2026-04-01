package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;

public class AssignCaseRequest {

    @NotNull(message = "handlingOfficerId is required")
    private Long handlingOfficerId;

    public Long getHandlingOfficerId() {
        return handlingOfficerId;
    }

    public void setHandlingOfficerId(Long handlingOfficerId) {
        this.handlingOfficerId = handlingOfficerId;
    }
}

