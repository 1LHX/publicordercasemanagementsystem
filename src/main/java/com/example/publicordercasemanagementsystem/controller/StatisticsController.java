package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.CasesOverviewResponse;
import com.example.publicordercasemanagementsystem.dto.OfficerEfficiencyItem;
import com.example.publicordercasemanagementsystem.dto.RegionHotspotItem;
import com.example.publicordercasemanagementsystem.dto.ReviewPassRateItem;
import com.example.publicordercasemanagementsystem.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/cases-overview")
    public ResponseEntity<ApiResponse<CasesOverviewResponse>> casesOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "DAY") String granularity) {
        CasesOverviewResponse response = statisticsService.getCasesOverview(startTime, endTime, granularity);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/region-hotspots")
    public ResponseEntity<ApiResponse<List<RegionHotspotItem>>> regionHotspots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer topN) {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getRegionHotspots(startTime, endTime, topN)));
    }

    @GetMapping("/officer-efficiency")
    public ResponseEntity<ApiResponse<List<OfficerEfficiencyItem>>> officerEfficiency(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer topN) {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getOfficerEfficiency(startTime, endTime, topN)));
    }

    @GetMapping("/review-pass-rate")
    public ResponseEntity<ApiResponse<List<ReviewPassRateItem>>> reviewPassRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getReviewPassRate(startTime, endTime)));
    }
}

