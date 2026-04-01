package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.AssignCaseRequest;
import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import com.example.publicordercasemanagementsystem.dto.CaseEvidenceItem;
import com.example.publicordercasemanagementsystem.dto.CaseListItem;
import com.example.publicordercasemanagementsystem.dto.CaseProcessItem;
import com.example.publicordercasemanagementsystem.dto.CreateCaseRequest;
import com.example.publicordercasemanagementsystem.dto.CreateEvidenceRequest;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateCaseRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.CaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        CaseDetailResponse response = caseService.createCase(request, getCurrentUserName(), httpRequest);
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.getCaseById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> updateCase(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateCaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.updateCase(id, request), "Case updated successfully"));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> acceptCase(@PathVariable Long id,
                                                                      HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.acceptCase(id, getCurrentUserName(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case accepted successfully"));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> assignCase(@PathVariable Long id,
                                                                      @Valid @RequestBody AssignCaseRequest request,
                                                                      HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.assignCase(id, request, getCurrentUserName(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case assigned successfully"));
    }

    @PostMapping("/{id}/status-transitions")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> transitionCaseStatus(@PathVariable Long id,
                                                                                 @Valid @RequestBody StatusTransitionRequest request,
                                                                                 HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.transitionStatus(id, request, getCurrentUserName(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case status updated successfully"));
    }

    @GetMapping("/{id}/processes")
    public ResponseEntity<ApiResponse<List<CaseProcessItem>>> listCaseProcesses(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listProcesses(id)));
    }

    @PostMapping("/{id}/evidences")
    public ResponseEntity<ApiResponse<CaseEvidenceItem>> addEvidence(@PathVariable Long id,
                                                                     @Valid @RequestBody CreateEvidenceRequest request) {
        CaseEvidenceItem response = caseService.addEvidence(id, request, getCurrentUserName());
        return ResponseEntity.ok(ApiResponse.ok(response, "Evidence added successfully"));
    }

    @GetMapping("/{id}/evidences")
    public ResponseEntity<ApiResponse<List<CaseEvidenceItem>>> listCaseEvidences(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(caseService.listEvidences(id)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<CaseDetailResponse>> archiveCase(@PathVariable Long id,
                                                                       HttpServletRequest httpRequest) {
        CaseDetailResponse response = caseService.archiveCase(id, getCurrentUserName(), httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(response, "Case archived successfully"));
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthException(401, "Unauthenticated");
        }
        return authentication.getName();
    }
}

