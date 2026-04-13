package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.AssignCaseRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import com.example.publicordercasemanagementsystem.dto.CreateCaseRequest;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.service.CaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CaseControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private CaseService caseService;

    @InjectMocks
    private CaseController caseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(caseController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCaseShouldReturnCreatedEnvelope() throws Exception {
        CaseDetailResponse response = buildCaseDetail(101L, "GA-2026-0001", "REGISTERED");
        when(caseService.createCase(any(CreateCaseRequest.class), eq(1L), any())).thenReturn(response);

        String requestBody = """
                {
                  "caseNumber": "GA-2026-0001",
                  "title": "街面纠纷警情",
                  "typeCode": "DISPUTE",
                  "departmentId": 2
                }
                """;

        mockMvc.perform(post("/api/cases")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case created successfully"))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));
    }

    @Test
    void createCaseShouldReturnValidationFailedWhenRequiredFieldMissing() throws Exception {
        String requestBody = """
                {
                  "title": "街面纠纷警情",
                  "typeCode": "DISPUTE",
                  "departmentId": 2
                }
                """;

        mockMvc.perform(post("/api/cases")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void acceptCaseShouldReturnAcceptedEnvelope() throws Exception {
        CaseDetailResponse response = buildCaseDetail(101L, "GA-2026-0001", "ACCEPTED");
        when(caseService.acceptCase(eq(101L), eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/cases/101/accept").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case accepted successfully"))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void deleteCasesShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/cases/batch")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"ids\":[101,102]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cases deleted successfully"));
    }

    @Test
    void assignCaseShouldReturnAssignedEnvelope() throws Exception {
        CaseDetailResponse response = buildCaseDetail(101L, "GA-2026-0001", "ACCEPTED");
        response.setHandlingOfficerId(6L);
        when(caseService.assignCase(eq(101L), any(AssignCaseRequest.class), eq(1L), any())).thenReturn(response);

        String requestBody = """
                {
                  "handlingOfficerId": 6
                }
                """;

        mockMvc.perform(post("/api/cases/101/assign")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case assigned successfully"))
                .andExpect(jsonPath("$.data.handlingOfficerId").value(6));
    }

    @Test
    void archiveCaseShouldReturnArchivedEnvelope() throws Exception {
        CaseDetailResponse response = buildCaseDetail(101L, "GA-2026-0001", "ARCHIVED");
        when(caseService.archiveCase(eq(101L), eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/cases/101/archive").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case archived successfully"))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    void acceptCaseShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/accept"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, List.of())
            );
            return request;
        };
    }

    private CaseDetailResponse buildCaseDetail(Long id, String caseNumber, String status) {
        CaseDetailResponse response = new CaseDetailResponse();
        response.setId(id);
        response.setCaseNumber(caseNumber);
        response.setStatus(status);
        response.setTitle("街面纠纷警情");
        response.setTypeCode("DISPUTE");
        response.setDepartmentId(2L);
        return response;
    }
}

