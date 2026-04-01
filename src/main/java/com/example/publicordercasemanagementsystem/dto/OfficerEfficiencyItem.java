package com.example.publicordercasemanagementsystem.dto;

public class OfficerEfficiencyItem {

    private Long officerId;
    private String officerName;
    private Long assignedCount;
    private Long completedCount;
    private Long overdueCount;
    private Double completionRate;

    public Long getOfficerId() {
        return officerId;
    }

    public void setOfficerId(Long officerId) {
        this.officerId = officerId;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public Long getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(Long assignedCount) {
        this.assignedCount = assignedCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public Long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }
}

