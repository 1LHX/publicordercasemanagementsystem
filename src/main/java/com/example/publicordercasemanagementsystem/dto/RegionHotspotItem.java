package com.example.publicordercasemanagementsystem.dto;

public class RegionHotspotItem {

    private String region;
    private Long caseCount;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(Long caseCount) {
        this.caseCount = caseCount;
    }
}

