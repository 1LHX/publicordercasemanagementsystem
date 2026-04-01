package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.AssignCaseRequest;
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
import com.example.publicordercasemanagementsystem.dto.UpdateCaseRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CaseService {

    CaseDetailResponse createCase(CreateCaseRequest request, String operatorName, HttpServletRequest httpRequest);

    PageResult<CaseListItem> listCases(String caseNumber,
                                       String title,
                                       String typeCode,
                                       String status,
                                       Long departmentId,
                                       Long handlingOfficerId,
                                       Boolean isOverdue,
                                       Integer page,
                                       Integer size);

    PageResult<CaseListItem> listArchivedCases(Integer page, Integer size);

    PageResult<CaseListItem> listDeadlineWarnings(Integer withinDays, Integer page, Integer size);

    PageResult<CaseListItem> listOverdueCases(Integer page, Integer size);

    CaseDetailResponse getCaseById(Long id);

    CaseExportResponse exportCase(Long id);

    CaseDetailResponse updateCase(Long id, UpdateCaseRequest request);

    CaseDetailResponse acceptCase(Long id, String operatorName, HttpServletRequest httpRequest);

    CaseDetailResponse assignCase(Long id, AssignCaseRequest request, String operatorName, HttpServletRequest httpRequest);

    CaseDetailResponse transitionStatus(Long id, StatusTransitionRequest request, String operatorName, HttpServletRequest httpRequest);

    List<CaseProcessItem> listProcesses(Long id);

    CaseEvidenceItem addEvidence(Long id, CreateEvidenceRequest request, String operatorName);

    List<CaseEvidenceItem> listEvidences(Long id);

    CaseDetailResponse submitLegalReview(Long id,
                                         LegalReviewSubmitRequest request,
                                         String operatorName,
                                         HttpServletRequest httpRequest);

    CaseDetailResponse approveLegalReview(Long id,
                                          LegalReviewApproveRequest request,
                                          String operatorName,
                                          HttpServletRequest httpRequest);

    CaseDetailResponse rejectLegalReview(Long id,
                                         LegalReviewRejectRequest request,
                                         String operatorName,
                                         HttpServletRequest httpRequest);

    CaseDetailResponse saveDecision(Long id,
                                    SaveDecisionRequest request,
                                    String operatorName,
                                    HttpServletRequest httpRequest);

    CaseDetailResponse recordExecution(Long id,
                                       RecordExecutionRequest request,
                                       String operatorName,
                                       HttpServletRequest httpRequest);

    CaseDetailResponse archiveCase(Long id, String operatorName, HttpServletRequest httpRequest);

    CaseDetailResponse unarchiveCase(Long id, String operatorName, HttpServletRequest httpRequest);
}
