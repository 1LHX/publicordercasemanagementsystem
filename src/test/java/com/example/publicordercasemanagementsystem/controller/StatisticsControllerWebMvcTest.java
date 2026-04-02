package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.CasesOverviewResponse;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.StatisticsService;
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
}

