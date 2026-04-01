package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.CasesOverviewResponse;
import com.example.publicordercasemanagementsystem.dto.OfficerEfficiencyItem;
import com.example.publicordercasemanagementsystem.dto.RegionHotspotItem;
import com.example.publicordercasemanagementsystem.dto.ReviewPassRateItem;
import com.example.publicordercasemanagementsystem.mapper.StatisticsMapper;
import com.example.publicordercasemanagementsystem.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int DEFAULT_TOP_N = 10;

    private final StatisticsMapper statisticsMapper;

    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public CasesOverviewResponse getCasesOverview(LocalDateTime startTime,
                                                  LocalDateTime endTime,
                                                  String granularity) {
        String periodFormat = "MONTH".equalsIgnoreCase(granularity) ? "%Y-%m" : "%Y-%m-%d";

        CasesOverviewResponse response = new CasesOverviewResponse();
        response.setTotalCases(statisticsMapper.countCasesTotal(startTime, endTime));
        response.setByStatus(statisticsMapper.countCasesByStatus(startTime, endTime));
        response.setByType(statisticsMapper.countCasesByType(startTime, endTime));
        response.setByPeriod(statisticsMapper.countCasesByPeriod(startTime, endTime, periodFormat));
        return response;
    }

    @Override
    public List<RegionHotspotItem> getRegionHotspots(LocalDateTime startTime,
                                                      LocalDateTime endTime,
                                                      Integer topN) {
        int safeTopN = topN == null || topN < 1 ? DEFAULT_TOP_N : topN;
        return statisticsMapper.findRegionHotspots(startTime, endTime, safeTopN);
    }

    @Override
    public List<OfficerEfficiencyItem> getOfficerEfficiency(LocalDateTime startTime,
                                                            LocalDateTime endTime,
                                                            Integer topN) {
        int safeTopN = topN == null || topN < 1 ? DEFAULT_TOP_N : topN;
        return statisticsMapper.findOfficerEfficiency(startTime, endTime, safeTopN);
    }

    @Override
    public List<ReviewPassRateItem> getReviewPassRate(LocalDateTime startTime,
                                                      LocalDateTime endTime) {
        return statisticsMapper.findReviewPassRate(startTime, endTime);
    }
}

