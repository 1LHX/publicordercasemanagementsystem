package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.CasesOverviewResponse;
import com.example.publicordercasemanagementsystem.dto.OfficerEfficiencyItem;
import com.example.publicordercasemanagementsystem.dto.RegionHotspotItem;
import com.example.publicordercasemanagementsystem.dto.ReviewPassRateItem;
import com.example.publicordercasemanagementsystem.dto.TimeCountItem;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {

    CasesOverviewResponse getCasesOverview(LocalDateTime startTime,
                                           LocalDateTime endTime,
                                           String granularity);

    List<RegionHotspotItem> getRegionHotspots(LocalDateTime startTime,
                                              LocalDateTime endTime,
                                              Integer topN);

    List<OfficerEfficiencyItem> getOfficerEfficiency(LocalDateTime startTime,
                                                     LocalDateTime endTime,
                                                     Integer topN);

    List<ReviewPassRateItem> getReviewPassRate(LocalDateTime startTime,
                                               LocalDateTime endTime);

    List<TimeCountItem> getCreatedCasesTrend(LocalDateTime startTime,
                                             LocalDateTime endTime,
                                             String granularity);

    long countCurrentOnlineUsers();

    long countTotalUsers();

    long countPoliceOfficers();

    long countTotalCases();

    long countOpenCases();

    long countClosedCases();

    long countOverdueCases();

    long countNearDeadlineCases(Integer withinDays);
}

