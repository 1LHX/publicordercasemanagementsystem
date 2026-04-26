package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;

public class CaseDocumentRequest {

    @NotNull(message = "caseId is required")
    private Long caseId;

    private String model;

    private String documentType; // e.g., "DECISION", "NOTICE", "REPORT"

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
