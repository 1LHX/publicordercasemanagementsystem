package com.example.publicordercasemanagementsystem.service;

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

    CaseDetailResponse getCaseById(Long id);

    CaseDetailResponse updateCase(Long id, UpdateCaseRequest request);

    CaseDetailResponse acceptCase(Long id, String operatorName, HttpServletRequest httpRequest);

    CaseDetailResponse assignCase(Long id, AssignCaseRequest request, String operatorName, HttpServletRequest httpRequest);

    CaseDetailResponse transitionStatus(Long id, StatusTransitionRequest request, String operatorName, HttpServletRequest httpRequest);

    List<CaseProcessItem> listProcesses(Long id);

    CaseEvidenceItem addEvidence(Long id, CreateEvidenceRequest request, String operatorName);

    List<CaseEvidenceItem> listEvidences(Long id);

    CaseDetailResponse archiveCase(Long id, String operatorName, HttpServletRequest httpRequest);
}

