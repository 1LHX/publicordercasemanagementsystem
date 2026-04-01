package com.example.publicordercasemanagementsystem.dto;

import java.time.LocalDateTime;

public class CaseListItem {

    private Long id;
    private String caseNumber;
    private String title;
    private String typeCode;
    private String typeName;
    private String status;
    private Long handlingOfficerId;
    private String handlingOfficerName;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime deadlineTime;
    private Boolean isOverdue;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getHandlingOfficerId() {
        return handlingOfficerId;
    }

    public void setHandlingOfficerId(Long handlingOfficerId) {
        this.handlingOfficerId = handlingOfficerId;
    }

    public String getHandlingOfficerName() {
        return handlingOfficerName;
    }

    public void setHandlingOfficerName(String handlingOfficerName) {
        this.handlingOfficerName = handlingOfficerName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean overdue) {
        isOverdue = overdue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

