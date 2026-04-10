package com.example.publicordercasemanagementsystem.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowInstanceResponse {

    private Long id;
    private Long caseId;
    private String flowType;
    private String status;
    private String currentNodeKey;
    private Long startedBy;
    private String startedByName;
    private LocalDateTime startedAt;
    private Long finishedBy;
    private String finishedByName;
    private LocalDateTime finishedAt;
    private List<WorkflowTaskItem> tasks;

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

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentNodeKey() {
        return currentNodeKey;
    }

    public void setCurrentNodeKey(String currentNodeKey) {
        this.currentNodeKey = currentNodeKey;
    }

    public Long getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(Long startedBy) {
        this.startedBy = startedBy;
    }

    public String getStartedByName() {
        return startedByName;
    }

    public void setStartedByName(String startedByName) {
        this.startedByName = startedByName;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public Long getFinishedBy() {
        return finishedBy;
    }

    public void setFinishedBy(Long finishedBy) {
        this.finishedBy = finishedBy;
    }

    public String getFinishedByName() {
        return finishedByName;
    }

    public void setFinishedByName(String finishedByName) {
        this.finishedByName = finishedByName;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<WorkflowTaskItem> getTasks() {
        return tasks;
    }

    public void setTasks(List<WorkflowTaskItem> tasks) {
        this.tasks = tasks;
    }
}

