package com.example.publicordercasemanagementsystem.dto;

import java.time.LocalDateTime;

public class WorkflowTaskItem {

    private Long id;
    private Long instanceId;
    private String nodeKey;
    private String nodeName;
    private String assigneeRole;
    private String status;
    private String comment;
    private Long actedBy;
    private String actedByName;
    private LocalDateTime actedAt;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAssigneeRole() {
        return assigneeRole;
    }

    public void setAssigneeRole(String assigneeRole) {
        this.assigneeRole = assigneeRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getActedBy() {
        return actedBy;
    }

    public void setActedBy(Long actedBy) {
        this.actedBy = actedBy;
    }

    public String getActedByName() {
        return actedByName;
    }

    public void setActedByName(String actedByName) {
        this.actedByName = actedByName;
    }

    public LocalDateTime getActedAt() {
        return actedAt;
    }

    public void setActedAt(LocalDateTime actedAt) {
        this.actedAt = actedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

