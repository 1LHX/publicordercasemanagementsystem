package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import com.example.publicordercasemanagementsystem.dto.CaseEvidenceItem;
import com.example.publicordercasemanagementsystem.dto.CaseExportResponse;
import com.example.publicordercasemanagementsystem.dto.CaseListItem;
import com.example.publicordercasemanagementsystem.dto.CaseProcessItem;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.CaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CaseControllerBranchWebMvcTest {

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
    void createCaseShouldReturnBusinessError() throws Exception {
        when(caseService.createCase(any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Case number already exists"));

        mockMvc.perform(post("/api/cases")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateCaseRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void listCasesShouldReturnOk() throws Exception {
        when(caseService.listCases(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(new CaseListItem()), 1, 1, 10));

        mockMvc.perform(get("/api/cases").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listCasesShouldReturnBusinessError() throws Exception {
        when(caseService.listCases(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new AuthException(500, "Case query failed"));

        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void listArchivedCasesShouldReturnOk() throws Exception {
        when(caseService.listArchivedCases(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/api/cases/archived"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listArchivedCasesShouldReturnBusinessError() throws Exception {
        when(caseService.listArchivedCases(any(), any()))
                .thenThrow(new AuthException(500, "Archive query failed"));

        mockMvc.perform(get("/api/cases/archived"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void listDeadlineWarningsShouldReturnOk() throws Exception {
        when(caseService.listDeadlineWarnings(any(), any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/api/cases/deadline-warnings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listDeadlineWarningsShouldReturnBusinessError() throws Exception {
        when(caseService.listDeadlineWarnings(any(), any(), any()))
                .thenThrow(new AuthException(500, "Deadline query failed"));

        mockMvc.perform(get("/api/cases/deadline-warnings"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void listOverdueCasesShouldReturnOk() throws Exception {
        when(caseService.listOverdueCases(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 1, 10));

        mockMvc.perform(get("/api/cases/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listOverdueCasesShouldReturnBusinessError() throws Exception {
        when(caseService.listOverdueCases(any(), any()))
                .thenThrow(new AuthException(500, "Overdue query failed"));

        mockMvc.perform(get("/api/cases/overdue"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void getCaseByIdShouldReturnOk() throws Exception {
        when(caseService.getCaseById(101L)).thenReturn(buildCaseDetail("REGISTERED"));

        mockMvc.perform(get("/api/cases/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getCaseByIdShouldReturnBusinessError() throws Exception {
        when(caseService.getCaseById(101L)).thenThrow(new AuthException(404, "Case not found"));

        mockMvc.perform(get("/api/cases/101"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void exportCaseShouldReturnOk() throws Exception {
        CaseExportResponse response = new CaseExportResponse();
        response.setCaseId(101L);
        response.setFileName("case-101.pdf");
        when(caseService.exportCase(101L)).thenReturn(response);

        mockMvc.perform(get("/api/cases/101/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case dossier exported"));
    }

    @Test
    void exportCaseShouldReturnBusinessError() throws Exception {
        when(caseService.exportCase(101L)).thenThrow(new AuthException(404, "Case not found"));

        mockMvc.perform(get("/api/cases/101/export"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateCaseShouldReturnOk() throws Exception {
        when(caseService.updateCase(eq(101L), any(), eq(1L))).thenReturn(buildCaseDetail("UPDATED"));

        mockMvc.perform(put("/api/cases/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateCaseRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateCaseShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(put("/api/cases/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updateCaseShouldReturnBusinessError() throws Exception {
        when(caseService.updateCase(eq(101L), any(), eq(1L))).thenThrow(new AuthException(409, "Cannot update archived case"));

        mockMvc.perform(put("/api/cases/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateCaseRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void deleteCaseShouldReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/cases/101")
                        .with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Case deleted successfully"));
    }

    @Test
    void deleteCaseShouldReturnBusinessError() throws Exception {
        org.mockito.Mockito.doThrow(new AuthException(404, "Case not found"))
                .when(caseService).deleteCase(eq(101L), eq(1L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/cases/101")
                        .with(authenticatedUser(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void transitionStatusShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/status-transitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"IN_REVIEW\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void transitionStatusShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/status-transitions")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void transitionStatusShouldReturnBusinessError() throws Exception {
        when(caseService.transitionStatus(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Illegal status transition"));

        mockMvc.perform(post("/api/cases/101/status-transitions")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toStatus\":\"IN_REVIEW\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void listCaseProcessesShouldReturnOk() throws Exception {
        CaseProcessItem item = new CaseProcessItem();
        item.setId(1L);
        when(caseService.listProcesses(101L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/cases/101/processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listCaseProcessesShouldReturnBusinessError() throws Exception {
        when(caseService.listProcesses(101L)).thenThrow(new AuthException(404, "Case not found"));

        mockMvc.perform(get("/api/cases/101/processes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void addEvidenceShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/evidences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvidenceRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void addEvidenceShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/evidences")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addEvidenceShouldReturnBusinessError() throws Exception {
        when(caseService.addEvidence(eq(101L), any(), eq(1L)))
                .thenThrow(new AuthException(409, "Evidence already exists"));

        mockMvc.perform(post("/api/cases/101/evidences")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEvidenceRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void updateEvidenceShouldReturnOk() throws Exception {
        CaseEvidenceItem item = new CaseEvidenceItem();
        item.setId(11L);
        when(caseService.updateEvidence(eq(101L), eq(11L), any(), eq(1L))).thenReturn(item);

        mockMvc.perform(put("/api/cases/101/evidences/11")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateEvidenceRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Evidence updated successfully"));
    }

    @Test
    void deleteEvidenceShouldReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/cases/101/evidences/11")
                        .with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Evidence deleted successfully"));
    }

    @Test
    void listCaseEvidencesShouldReturnOk() throws Exception {
        CaseEvidenceItem item = new CaseEvidenceItem();
        item.setId(1L);
        when(caseService.listEvidences(101L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/cases/101/evidences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listCaseEvidencesShouldReturnBusinessError() throws Exception {
        when(caseService.listEvidences(101L)).thenThrow(new AuthException(404, "Case not found"));

        mockMvc.perform(get("/api/cases/101/evidences"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void submitLegalReviewShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"ok\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void submitLegalReviewShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/submit")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void submitLegalReviewShouldReturnBusinessError() throws Exception {
        when(caseService.submitLegalReview(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Review already submitted"));

        mockMvc.perform(post("/api/cases/101/legal-review/submit")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"ok\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void approveLegalReviewShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"approved\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void approveLegalReviewShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/approve")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void approveLegalReviewShouldReturnBusinessError() throws Exception {
        when(caseService.approveLegalReview(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Case not in review"));

        mockMvc.perform(post("/api/cases/101/legal-review/approve")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"approved\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void rejectLegalReviewShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"lack evidence\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectLegalReviewShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/legal-review/reject")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectLegalReviewShouldReturnBusinessError() throws Exception {
        when(caseService.rejectLegalReview(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Case not in review"));

        mockMvc.perform(post("/api/cases/101/legal-review/reject")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"lack evidence\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void saveDecisionShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDecisionRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void saveDecisionShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/decision")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void saveDecisionShouldReturnBusinessError() throws Exception {
        when(caseService.saveDecision(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Decision not allowed"));

        mockMvc.perform(post("/api/cases/101/decision")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDecisionRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void recordExecutionShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/execution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"executionResult\":\"DONE\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void recordExecutionShouldReturnValidationFailed() throws Exception {
        mockMvc.perform(post("/api/cases/101/execution")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void recordExecutionShouldReturnBusinessError() throws Exception {
        when(caseService.recordExecution(eq(101L), any(), eq(1L), any()))
                .thenThrow(new AuthException(409, "Execution not allowed"));

        mockMvc.perform(post("/api/cases/101/execution")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"executionResult\":\"DONE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void unarchiveCaseShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/cases/101/unarchive"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void unarchiveCaseShouldReturnBusinessError() throws Exception {
        when(caseService.unarchiveCase(eq(101L), eq(1L), any()))
                .thenThrow(new AuthException(409, "Case is not archived"));

        mockMvc.perform(post("/api/cases/101/unarchive").with(authenticatedUser(1L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, List.of())
            );
            return request;
        };
    }

    private CaseDetailResponse buildCaseDetail(String status) {
        CaseDetailResponse response = new CaseDetailResponse();
        response.setId(101L);
        response.setCaseNumber("GA-2026-0001");
        response.setStatus(status);
        return response;
    }

    private String validCreateCaseRequest() {
        return """
                {
                  "caseNumber": "GA-2026-0001",
                  "title": "Street dispute",
                  "typeCode": "DISPUTE",
                  "departmentId": 2
                }
                """;
    }

    private String validUpdateCaseRequest() {
        return """
                {
                  "title": "Updated case title",
                  "typeCode": "DISPUTE",
                  "departmentId": 2
                }
                """;
    }

    private String validEvidenceRequest() {
        return """
                {
                  "fileName": "video.mp4",
                  "filePath": "/evidence/video.mp4"
                }
                """;
    }

    private String validUpdateEvidenceRequest() {
        return """
                {
                  "fileName": "updated-video.mp4",
                  "filePath": "/evidence/updated-video.mp4",
                  "description": "updated"
                }
                """;
    }

    private String validDecisionRequest() {
        return """
                {
                  "decisionResult": "FINE",
                  "decisionContent": "Administrative fine"
                }
                """;
    }
}

