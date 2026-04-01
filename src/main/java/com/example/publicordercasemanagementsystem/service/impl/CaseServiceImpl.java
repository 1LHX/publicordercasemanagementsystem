package com.example.publicordercasemanagementsystem.service.impl;

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
import com.example.publicordercasemanagementsystem.mapper.CaseMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.CaseEvidence;
import com.example.publicordercasemanagementsystem.pojo.CaseProcess;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.CaseService;
import com.example.publicordercasemanagementsystem.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CaseServiceImpl implements CaseService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final CaseMapper caseMapper;
    private final UserMapper userMapper;

    public CaseServiceImpl(CaseMapper caseMapper, UserMapper userMapper) {
        this.caseMapper = caseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public CaseDetailResponse createCase(CreateCaseRequest request, String operatorName, HttpServletRequest httpRequest) {
        if (caseMapper.findByCaseNumber(request.getCaseNumber()) != null) {
            throw new AuthException(400, "Case number already exists");
        }

        CaseRecord record = new CaseRecord();
        record.setCaseNumber(request.getCaseNumber());
        record.setTitle(request.getTitle());
        record.setTypeCode(request.getTypeCode());
        record.setStatus(STATUS_REGISTERED);
        record.setReporterName(request.getReporterName());
        record.setReporterContact(request.getReporterContact());
        record.setIncidentTime(request.getIncidentTime());
        record.setIncidentLocation(request.getIncidentLocation());
        record.setBriefDescription(request.getBriefDescription());
        record.setDepartmentId(request.getDepartmentId());
        record.setDeadlineTime(request.getDeadlineTime());
        record.setIsOverdue(false);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        caseMapper.insertCase(record);

        addProcess(record.getId(), null, STATUS_REGISTERED, operatorName, "Case created", httpRequest);
        return toDetail(requireCase(record.getId()));
    }

    @Override
    public PageResult<CaseListItem> listCases(String caseNumber,
                                              String title,
                                              String typeCode,
                                              String status,
                                              Long departmentId,
                                              Long handlingOfficerId,
                                              Boolean isOverdue,
                                              Integer page,
                                              Integer size) {
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;

        long total = caseMapper.countCases(caseNumber, title, typeCode, status, departmentId, handlingOfficerId, isOverdue);
        if (total == 0) {
            return new PageResult<>(new ArrayList<>(), 0, safePage, safeSize);
        }

        List<CaseRecord> records = caseMapper.findCasePage(
                caseNumber, title, typeCode, status, departmentId, handlingOfficerId, isOverdue, offset, safeSize
        );

        List<CaseListItem> items = new ArrayList<>(records.size());
        for (CaseRecord record : records) {
            items.add(toListItem(record));
        }
        return new PageResult<>(items, total, safePage, safeSize);
    }

    @Override
    public CaseDetailResponse getCaseById(Long id) {
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse updateCase(Long id, UpdateCaseRequest request) {
        CaseRecord existing = requireCase(id);
        existing.setTitle(request.getTitle());
        existing.setTypeCode(request.getTypeCode());
        existing.setReporterName(request.getReporterName());
        existing.setReporterContact(request.getReporterContact());
        existing.setIncidentTime(request.getIncidentTime());
        existing.setIncidentLocation(request.getIncidentLocation());
        existing.setBriefDescription(request.getBriefDescription());
        existing.setDepartmentId(request.getDepartmentId());
        existing.setDeadlineTime(request.getDeadlineTime());
        caseMapper.updateCase(existing);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse acceptCase(Long id, String operatorName, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        caseMapper.updateCaseStatus(id, STATUS_ACCEPTED, LocalDateTime.now());
        addProcess(id, record.getStatus(), STATUS_ACCEPTED, operatorName, "Case accepted", httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse assignCase(Long id, AssignCaseRequest request, String operatorName, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        User officer = userMapper.findById(request.getHandlingOfficerId());
        if (officer == null || !Boolean.TRUE.equals(officer.getIsActive())) {
            throw new AuthException(400, "Handling officer not found or inactive");
        }
        caseMapper.assignOfficer(id, request.getHandlingOfficerId(), officer.getDepartmentId());
        addProcess(id, record.getStatus(), record.getStatus(), operatorName, "Assigned handling officer", httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse transitionStatus(Long id, StatusTransitionRequest request, String operatorName, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        String fromStatus = record.getStatus();
        String toStatus = request.getToStatus();
        if (fromStatus != null && fromStatus.equals(toStatus)) {
            throw new AuthException(400, "Target status must be different from current status");
        }
        LocalDateTime acceptanceTime = record.getAcceptanceTime();
        if (STATUS_ACCEPTED.equals(toStatus) && acceptanceTime == null) {
            acceptanceTime = LocalDateTime.now();
        }
        caseMapper.updateCaseStatus(id, toStatus, acceptanceTime);
        addProcess(id, fromStatus, toStatus, operatorName, request.getComment(), httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public List<CaseProcessItem> listProcesses(Long id) {
        requireCase(id);
        List<CaseProcess> processes = caseMapper.findProcessesByCaseId(id);
        List<CaseProcessItem> items = new ArrayList<>(processes.size());
        for (CaseProcess process : processes) {
            items.add(toProcessItem(process));
        }
        return items;
    }

    @Override
    public CaseEvidenceItem addEvidence(Long id, CreateEvidenceRequest request, String operatorName) {
        requireCase(id);
        User operator = requireOperator(operatorName);

        CaseEvidence evidence = new CaseEvidence();
        evidence.setCaseId(id);
        evidence.setFileName(request.getFileName());
        evidence.setFilePath(request.getFilePath());
        evidence.setFileType(request.getFileType());
        evidence.setFileSize(request.getFileSize());
        evidence.setUploadUserId(operator.getId());
        evidence.setDescription(request.getDescription());
        evidence.setUploadedAt(LocalDateTime.now());
        caseMapper.insertEvidence(evidence);

        List<CaseEvidence> list = caseMapper.findEvidencesByCaseId(id);
        for (CaseEvidence item : list) {
            if (item.getId().equals(evidence.getId())) {
                return toEvidenceItem(item);
            }
        }
        throw new AuthException(500, "Failed to load created evidence");
    }

    @Override
    public List<CaseEvidenceItem> listEvidences(Long id) {
        requireCase(id);
        List<CaseEvidence> evidences = caseMapper.findEvidencesByCaseId(id);
        List<CaseEvidenceItem> items = new ArrayList<>(evidences.size());
        for (CaseEvidence evidence : evidences) {
            items.add(toEvidenceItem(evidence));
        }
        return items;
    }

    @Override
    public CaseDetailResponse archiveCase(Long id, String operatorName, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        caseMapper.updateCaseStatus(id, STATUS_ARCHIVED, record.getAcceptanceTime());
        addProcess(id, record.getStatus(), STATUS_ARCHIVED, operatorName, "Case archived", httpRequest);
        return toDetail(requireCase(id));
    }

    private void addProcess(Long caseId,
                            String fromStatus,
                            String toStatus,
                            String operatorName,
                            String comment,
                            HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        CaseProcess process = new CaseProcess();
        process.setCaseId(caseId);
        process.setFromStatus(fromStatus);
        process.setToStatus(toStatus);
        process.setOperatorId(operator.getId());
        process.setOperationTime(LocalDateTime.now());
        process.setComment(comment);
        process.setIpAddress(httpRequest == null ? null : RequestUtil.getClientIp(httpRequest));
        caseMapper.insertProcess(process);
    }

    private User requireOperator(String operatorName) {
        if (operatorName == null || operatorName.isBlank()) {
            throw new AuthException(401, "Unauthenticated");
        }
        User user = userMapper.findByName(operatorName);
        if (user == null) {
            throw new AuthException(401, "Operator not found");
        }
        return user;
    }

    private CaseRecord requireCase(Long id) {
        CaseRecord record = caseMapper.findById(id);
        if (record == null) {
            throw new AuthException(404, "Case not found");
        }
        return record;
    }

    private CaseListItem toListItem(CaseRecord record) {
        CaseListItem item = new CaseListItem();
        item.setId(record.getId());
        item.setCaseNumber(record.getCaseNumber());
        item.setTitle(record.getTitle());
        item.setTypeCode(record.getTypeCode());
        item.setTypeName(record.getTypeName());
        item.setStatus(record.getStatus());
        item.setHandlingOfficerId(record.getHandlingOfficerId());
        item.setHandlingOfficerName(record.getHandlingOfficerName());
        item.setDepartmentId(record.getDepartmentId());
        item.setDepartmentName(record.getDepartmentName());
        item.setDeadlineTime(record.getDeadlineTime());
        item.setIsOverdue(record.getIsOverdue());
        item.setCreatedAt(record.getCreatedAt());
        return item;
    }

    private CaseDetailResponse toDetail(CaseRecord record) {
        CaseDetailResponse response = new CaseDetailResponse();
        response.setId(record.getId());
        response.setCaseNumber(record.getCaseNumber());
        response.setTitle(record.getTitle());
        response.setTypeCode(record.getTypeCode());
        response.setTypeName(record.getTypeName());
        response.setStatus(record.getStatus());
        response.setReporterName(record.getReporterName());
        response.setReporterContact(record.getReporterContact());
        response.setIncidentTime(record.getIncidentTime());
        response.setIncidentLocation(record.getIncidentLocation());
        response.setBriefDescription(record.getBriefDescription());
        response.setHandlingOfficerId(record.getHandlingOfficerId());
        response.setHandlingOfficerName(record.getHandlingOfficerName());
        response.setDepartmentId(record.getDepartmentId());
        response.setDepartmentName(record.getDepartmentName());
        response.setAcceptanceTime(record.getAcceptanceTime());
        response.setDeadlineTime(record.getDeadlineTime());
        response.setIsOverdue(record.getIsOverdue());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }

    private CaseProcessItem toProcessItem(CaseProcess process) {
        CaseProcessItem item = new CaseProcessItem();
        item.setId(process.getId());
        item.setCaseId(process.getCaseId());
        item.setFromStatus(process.getFromStatus());
        item.setToStatus(process.getToStatus());
        item.setOperatorId(process.getOperatorId());
        item.setOperatorName(process.getOperatorName());
        item.setComment(process.getComment());
        item.setIpAddress(process.getIpAddress());
        item.setOperationTime(process.getOperationTime());
        return item;
    }

    private CaseEvidenceItem toEvidenceItem(CaseEvidence evidence) {
        CaseEvidenceItem item = new CaseEvidenceItem();
        item.setId(evidence.getId());
        item.setCaseId(evidence.getCaseId());
        item.setFileName(evidence.getFileName());
        item.setFilePath(evidence.getFilePath());
        item.setFileType(evidence.getFileType());
        item.setFileSize(evidence.getFileSize());
        item.setUploadUserId(evidence.getUploadUserId());
        item.setUploadUserName(evidence.getUploadUserName());
        item.setDescription(evidence.getDescription());
        item.setUploadedAt(evidence.getUploadedAt());
        return item;
    }
}

