package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.AssignCaseRequest;
import com.example.publicordercasemanagementsystem.dto.BatchDeleteRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import com.example.publicordercasemanagementsystem.dto.CaseEvidenceItem;
import com.example.publicordercasemanagementsystem.dto.CaseExportResponse;
import com.example.publicordercasemanagementsystem.dto.CaseListItem;
import com.example.publicordercasemanagementsystem.dto.CaseProcessItem;
import com.example.publicordercasemanagementsystem.dto.CreateCaseRequest;
import com.example.publicordercasemanagementsystem.dto.CreateEvidenceRequest;
import com.example.publicordercasemanagementsystem.dto.LegalReviewApproveRequest;
import com.example.publicordercasemanagementsystem.dto.LegalReviewRejectRequest;
import com.example.publicordercasemanagementsystem.dto.LegalReviewSubmitRequest;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.RecordExecutionRequest;
import com.example.publicordercasemanagementsystem.dto.SaveDecisionRequest;
import com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateEvidenceRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateCaseRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.CaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CaseDetailResponse>> createCase(@Valid @RequestBody CreateCaseRequest request,
                                                                      HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.createCase(request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<CaseListItem>>> listCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long handlingOfficerId,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<CaseListItem> result = caseService.listCases(
                caseNumber, title, typeCode, status, departmentId, handlingOfficerId, isOverdue, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<PageResult<CaseListItem>>> listArchivedCases(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listArchivedCases(page, size)));
    }

    @GetMapping("/deadline-warnings")
    public ResponseEntity<ApiResponse<PageResult<CaseListItem>>> listDeadlineWarnings(
            @RequestParam(required = false) Integer withinDays,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listDeadlineWarnings(withinDays, page, size)));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<PageResult<CaseListItem>>> listOverdueCases(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listOverdueCases(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.getCaseById(id)));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<ApiResponse<CaseExportResponse>> exportCase(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.exportCase(id), "Case dossier exported"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> updateCase(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateCaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.updateCase(id, request), "Case updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCase(@PathVariable Long id) {
        caseService.deleteCase(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Case deleted successfully"));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteCases(@Valid @RequestBody BatchDeleteRequest request) {
        caseService.deleteCases(request.getIds(), getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Cases deleted successfully"));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> acceptCase(@PathVariable Long id,
                                                                      HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.acceptCase(id, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case accepted successfully"));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> assignCase(@PathVariable Long id,
                                                                      @Valid @RequestBody AssignCaseRequest request,
                                                                      HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.assignCase(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case assigned successfully"));
    }

    @PostMapping("/{id}/status-transitions")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> transitionCaseStatus(@PathVariable Long id,
                                                                                 @Valid @RequestBody StatusTransitionRequest request,
                                                                                 HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.transitionStatus(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case status updated successfully"));
    }

    @GetMapping("/{id}/processes")
    public ResponseEntity<ApiResponse<List<CaseProcessItem>>> listCaseProcesses(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listProcesses(id)));
    }

    @PostMapping("/{id}/evidences")
    public ResponseEntity<ApiResponse<CaseEvidenceItem>> addEvidence(@PathVariable Long id,
                                                                     @Valid @RequestBody CreateEvidenceRequest request) {
        CaseEvidenceItem response = caseService.addEvidence(id, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Evidence added successfully"));
    }

    @PutMapping("/{caseId}/evidences/{evidenceId}")
    public ResponseEntity<ApiResponse<CaseEvidenceItem>> updateEvidence(@PathVariable Long caseId,
                                                                        @PathVariable Long evidenceId,
                                                                        @Valid @RequestBody UpdateEvidenceRequest request) {
        CaseEvidenceItem response = caseService.updateEvidence(caseId, evidenceId, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Evidence updated successfully"));
    }

    @DeleteMapping("/{caseId}/evidences/{evidenceId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(@PathVariable Long caseId,
                                                            @PathVariable Long evidenceId) {
        caseService.deleteEvidence(caseId, evidenceId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Evidence deleted successfully"));
    }

    @GetMapping("/{id}/evidences")
    public ResponseEntity<ApiResponse<List<CaseEvidenceItem>>> listCaseEvidences(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listEvidences(id)));
    }

    @PostMapping("/{id}/legal-review/submit")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> submitLegalReview(@PathVariable Long id,
                                                                             @Valid @RequestBody LegalReviewSubmitRequest request,
                                                                             HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.submitLegalReview(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Legal review submitted successfully"));
    }

    @PostMapping("/{id}/legal-review/approve")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> approveLegalReview(@PathVariable Long id,
                                                                              @Valid @RequestBody LegalReviewApproveRequest request,
                                                                              HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.approveLegalReview(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Legal review approved successfully"));
    }

    @PostMapping("/{id}/legal-review/reject")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> rejectLegalReview(@PathVariable Long id,
                                                                             @Valid @RequestBody LegalReviewRejectRequest request,
                                                                             HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.rejectLegalReview(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Legal review rejected successfully"));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> saveDecision(@PathVariable Long id,
                                                                        @Valid @RequestBody SaveDecisionRequest request,
                                                                        HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.saveDecision(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Decision saved successfully"));
    }

    @PostMapping("/{id}/execution")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> recordExecution(@PathVariable Long id,
                                                                           @Valid @RequestBody RecordExecutionRequest request,
                                                                           HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.recordExecution(id, request, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Execution recorded successfully"));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> archiveCase(@PathVariable Long id,
                                                                       HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.archiveCase(id, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case archived successfully"));
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> unarchiveCase(@PathVariable Long id,
                                                                         HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.unarchiveCase(id, getCurrentUserId(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case unarchived successfully"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthException(401, "Unauthenticated");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new AuthException(401, "Invalid authentication principal");
        }
    }
}

