package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.CasesOverviewResponse;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.StatisticsService;
import com.example.publicordercasemanagementsystem.dto.TimeCountItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void casesOverviewShouldReturnOk() throws Exception {
        when(statisticsService.getCasesOverview(any(), any(), anyString())).thenReturn(new CasesOverviewResponse());

        mockMvc.perform(get("/api/statistics/cases-overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void casesOverviewShouldReturnBusinessError() throws Exception {
        when(statisticsService.getCasesOverview(any(), any(), anyString()))
                .thenThrow(new AuthException(422, "Invalid range"));

        mockMvc.perform(get("/api/statistics/cases-overview"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(422))
                .andExpect(jsonPath("$.message").value("Invalid range"));
    }

    @Test
    void regionHotspotsShouldReturnOk() throws Exception {
        when(statisticsService.getRegionHotspots(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/statistics/region-hotspots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void regionHotspotsShouldReturnBusinessError() throws Exception {
        when(statisticsService.getRegionHotspots(any(), any(), anyInt()))
                .thenThrow(new AuthException(409, "TopN out of range"));

        mockMvc.perform(get("/api/statistics/region-hotspots").param("topN", "5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("TopN out of range"));
    }

    @Test
    void officerEfficiencyShouldReturnOk() throws Exception {
        when(statisticsService.getOfficerEfficiency(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/statistics/officer-efficiency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void officerEfficiencyShouldReturnBusinessError() throws Exception {
        when(statisticsService.getOfficerEfficiency(any(), any(), anyInt()))
                .thenThrow(new AuthException(409, "TopN out of range"));

        mockMvc.perform(get("/api/statistics/officer-efficiency").param("topN", "3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("TopN out of range"));
    }

    @Test
    void reviewPassRateShouldReturnOk() throws Exception {
        when(statisticsService.getReviewPassRate(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/statistics/review-pass-rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void reviewPassRateShouldReturnBusinessError() throws Exception {
        when(statisticsService.getReviewPassRate(any(), any()))
                .thenThrow(new AuthException(500, "Stats query failed"));

        mockMvc.perform(get("/api/statistics/review-pass-rate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Stats query failed"));
    }

    @Test
    void acceptedCasesTrendShouldReturnOk() throws Exception {
        TimeCountItem item = new TimeCountItem();
        item.setPeriod("2026-04-01");
        item.setCount(6L);
        when(statisticsService.getAcceptedCasesTrend(any(), any(), anyString())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/statistics/accepted-cases-trend").param("granularity", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].period").value("2026-04-01"))
                .andExpect(jsonPath("$.data[0].count").value(6));
    }

    @Test
    void currentOnlineUsersShouldReturnOk() throws Exception {
        when(statisticsService.countCurrentOnlineUsers()).thenReturn(5L);

        mockMvc.perform(get("/api/statistics/current-online-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void totalUsersShouldReturnOk() throws Exception {
        when(statisticsService.countTotalUsers()).thenReturn(12L);

        mockMvc.perform(get("/api/statistics/total-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(12));
    }

    @Test
    void policeOfficersShouldReturnOk() throws Exception {
        when(statisticsService.countPoliceOfficers()).thenReturn(9L);

        mockMvc.perform(get("/api/statistics/police-officers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(9));
    }

    @Test
    void totalCasesShouldReturnOk() throws Exception {
        when(statisticsService.countTotalCases()).thenReturn(30L);

        mockMvc.perform(get("/api/statistics/total-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(30));
    }

    @Test
    void openCasesShouldReturnOk() throws Exception {
        when(statisticsService.countOpenCases()).thenReturn(20L);

        mockMvc.perform(get("/api/statistics/open-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(20));
    }

    @Test
    void closedCasesShouldReturnOk() throws Exception {
        when(statisticsService.countClosedCases()).thenReturn(10L);

        mockMvc.perform(get("/api/statistics/closed-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    void overdueCasesShouldReturnOk() throws Exception {
        when(statisticsService.countOverdueCases()).thenReturn(3L);

        mockMvc.perform(get("/api/statistics/overdue-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void nearDeadlineCasesShouldReturnOk() throws Exception {
        when(statisticsService.countNearDeadlineCases(any())).thenReturn(4L);

        mockMvc.perform(get("/api/statistics/near-deadline-cases").param("withinDays", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(4));
    }
}

