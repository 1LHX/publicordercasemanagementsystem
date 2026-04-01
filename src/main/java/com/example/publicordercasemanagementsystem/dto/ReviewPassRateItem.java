package com.example.publicordercasemanagementsystem.dto;

public class ReviewPassRateItem {

    private String period;
    private Long totalReviewed;
    private Long approvedCount;
    private Double passRate;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getTotalReviewed() {
        return totalReviewed;
    }

    public void setTotalReviewed(Long totalReviewed) {
        this.totalReviewed = totalReviewed;
    }

    public Long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(Long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }
}

