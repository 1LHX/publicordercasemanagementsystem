package com.example.publicordercasemanagementsystem.pojo;

import java.time.LocalDateTime;

public class CaseDecision {

    private Long id;
    private Long caseId;
    private String decisionResult;
    private String decisionContent;
    private String coerciveMeasureCode;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

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

    public Long getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(Long decidedBy) {
        this.decidedBy = decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

