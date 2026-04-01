package com.example.publicordercasemanagementsystem.dto;

import java.util.List;

public class CasesOverviewResponse {

    private Long totalCases;
    private List<StatisticsCountItem> byStatus;
    private List<StatisticsCountItem> byType;
    private List<TimeCountItem> byPeriod;

    public Long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(Long totalCases) {
        this.totalCases = totalCases;
    }

    public List<StatisticsCountItem> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<StatisticsCountItem> byStatus) {
        this.byStatus = byStatus;
    }

    public List<StatisticsCountItem> getByType() {
        return byType;
    }

    public void setByType(List<StatisticsCountItem> byType) {
        this.byType = byType;
    }

    public List<TimeCountItem> getByPeriod() {
        return byPeriod;
    }

    public void setByPeriod(List<TimeCountItem> byPeriod) {
        this.byPeriod = byPeriod;
    }
}

