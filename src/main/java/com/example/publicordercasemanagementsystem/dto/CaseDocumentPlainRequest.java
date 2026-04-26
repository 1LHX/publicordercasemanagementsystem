package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;

public class CaseDocumentPlainRequest {

    @NotNull(message = "caseId is required")
    private Long caseId;

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }
}
