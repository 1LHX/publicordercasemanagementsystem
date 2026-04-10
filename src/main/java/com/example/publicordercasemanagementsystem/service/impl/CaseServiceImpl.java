package com.example.publicordercasemanagementsystem.service.impl;

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
import com.example.publicordercasemanagementsystem.dto.StartCaseWorkflowRequest;
import com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateEvidenceRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateCaseRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowActionRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.CaseMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.CaseDecision;
import com.example.publicordercasemanagementsystem.pojo.CaseEvidence;
import com.example.publicordercasemanagementsystem.pojo.CaseExecution;
import com.example.publicordercasemanagementsystem.pojo.CaseLegalReview;
import com.example.publicordercasemanagementsystem.pojo.CaseProcess;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.CaseService;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import com.example.publicordercasemanagementsystem.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
public class CaseServiceImpl implements CaseService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_INVESTIGATING = "INVESTIGATING";
    private static final String STATUS_LEGAL_REVIEW = "LEGAL_REVIEW";
    private static final String STATUS_DECIDED = "DECIDED";
    private static final String STATUS_EXECUTED = "EXECUTED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private static final String REVIEW_SUBMITTED = "SUBMITTED";
    private static final String REVIEW_APPROVED = "APPROVED";
    private static final String REVIEW_REJECTED = "REJECTED";

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_WARNING_DAYS = 3;

    private final CaseMapper caseMapper;
    private final UserMapper userMapper;
    private final CaseWorkflowService caseWorkflowService;

    public CaseServiceImpl(CaseMapper caseMapper,
                           UserMapper userMapper,
                           CaseWorkflowService caseWorkflowService) {
        this.caseMapper = caseMapper;
        this.userMapper = userMapper;
        this.caseWorkflowService = caseWorkflowService;
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
    public PageResult<CaseListItem> listArchivedCases(Integer page, Integer size) {
        return listCases(null, null, null, STATUS_ARCHIVED, null, null, null, page, size);
    }

    @Override
    public PageResult<CaseListItem> listDeadlineWarnings(Integer withinDays, Integer page, Integer size) {
        caseMapper.refreshOverdueFlags(LocalDateTime.now());

        int safeDays = withinDays == null || withinDays < 1 ? DEFAULT_WARNING_DAYS : withinDays;
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;

        LocalDateTime deadlineBefore = LocalDateTime.now().plusDays(safeDays);
        long total = caseMapper.countDeadlineWarningCases(deadlineBefore);
        if (total == 0) {
            return new PageResult<>(new ArrayList<>(), 0, safePage, safeSize);
        }

        List<CaseRecord> records = caseMapper.findDeadlineWarningPage(deadlineBefore, offset, safeSize);
        return toPageResult(records, total, safePage, safeSize);
    }

    @Override
    public PageResult<CaseListItem> listOverdueCases(Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        caseMapper.refreshOverdueFlags(now);

        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;

        long total = caseMapper.countOverdueCases(now);
        if (total == 0) {
            return new PageResult<>(new ArrayList<>(), 0, safePage, safeSize);
        }

        List<CaseRecord> records = caseMapper.findOverdueCasePage(now, offset, safeSize);
        return toPageResult(records, total, safePage, safeSize);
    }

    @Override
    public CaseDetailResponse getCaseById(Long id) {
        return toDetail(requireCase(id));
    }

    @Override
    public CaseExportResponse exportCase(Long id) {
        CaseRecord record = requireCase(id);
        List<CaseProcess> processes = caseMapper.findProcessesByCaseId(id);
        List<CaseEvidence> evidences = caseMapper.findEvidencesByCaseId(id);

        String fileName = "case-" + record.getCaseNumber() + ".txt";
        String content = buildDossierContent(record, processes, evidences);

        CaseExportResponse response = new CaseExportResponse();
        response.setCaseId(record.getId());
        response.setCaseNumber(record.getCaseNumber());
        response.setFileName(fileName);
        response.setContentType("text/plain");
        response.setContentBase64(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
        response.setGeneratedAt(LocalDateTime.now());
        return response;
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
    public void deleteCase(Long id, String operatorName) {
        requireOperator(operatorName);
        requireCase(id);
        caseMapper.deleteCase(id);
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
    public CaseEvidenceItem updateEvidence(Long caseId, Long evidenceId, UpdateEvidenceRequest request, String operatorName) {
        requireOperator(operatorName);
        requireCase(caseId);
        CaseEvidence evidence = requireEvidence(caseId, evidenceId);
        evidence.setFileName(request.getFileName());
        evidence.setFilePath(request.getFilePath());
        evidence.setFileType(request.getFileType());
        evidence.setFileSize(request.getFileSize());
        evidence.setDescription(request.getDescription());
        caseMapper.updateEvidence(evidence);
        return toEvidenceItem(requireEvidence(caseId, evidenceId));
    }

    @Override
    public void deleteEvidence(Long caseId, Long evidenceId, String operatorName) {
        requireOperator(operatorName);
        requireCase(caseId);
        requireEvidence(caseId, evidenceId);
        caseMapper.deleteEvidence(evidenceId);
    }

    @Override
    public CaseDetailResponse submitLegalReview(Long id,
                                                LegalReviewSubmitRequest request,
                                                String operatorName,
                                                HttpServletRequest httpRequest) {
        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment(request.getComment());
        caseWorkflowService.startCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse approveLegalReview(Long id,
                                                 LegalReviewApproveRequest request,
                                                 String operatorName,
                                                 HttpServletRequest httpRequest) {
        WorkflowActionRequest workflowRequest = new WorkflowActionRequest();
        workflowRequest.setComment(request.getComment());
        caseWorkflowService.approveCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse rejectLegalReview(Long id,
                                                LegalReviewRejectRequest request,
                                                String operatorName,
                                                HttpServletRequest httpRequest) {
        WorkflowActionRequest workflowRequest = new WorkflowActionRequest();
        workflowRequest.setComment(request.getReason());
        caseWorkflowService.rejectCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse saveDecision(Long id,
                                           SaveDecisionRequest request,
                                           String operatorName,
                                           HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        LocalDateTime now = LocalDateTime.now();

        CaseDecision decision = new CaseDecision();
        decision.setCaseId(id);
        decision.setDecisionResult(request.getDecisionResult());
        decision.setDecisionContent(request.getDecisionContent());
        decision.setCoerciveMeasureCode(request.getCoerciveMeasureCode());
        decision.setDecidedBy(operator.getId());
        decision.setDecidedAt(request.getDecidedAt() == null ? now : request.getDecidedAt());
        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);
        caseMapper.upsertDecision(decision);

        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment("Decision submitted for approval");
        caseWorkflowService.startCaseWorkflow(id,
                "DECISION_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse recordExecution(Long id,
                                              RecordExecutionRequest request,
                                              String operatorName,
                                              HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        LocalDateTime now = LocalDateTime.now();

        CaseExecution execution = new CaseExecution();
        execution.setCaseId(id);
        execution.setExecutionResult(request.getExecutionResult());
        execution.setExecutionNote(request.getExecutionNote());
        execution.setExecutedBy(operator.getId());
        execution.setExecutedAt(request.getExecutedAt() == null ? now : request.getExecutedAt());
        execution.setCreatedAt(now);
        execution.setUpdatedAt(now);
        caseMapper.upsertExecution(execution);

        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment("Execution submitted for approval");
        caseWorkflowService.startCaseWorkflow(id,
                "EXECUTION_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse archiveCase(Long id, String operatorName, HttpServletRequest httpRequest) {
        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment("Archive submitted for approval");
        caseWorkflowService.startCaseWorkflow(id,
                "ARCHIVE_REVIEW",
                workflowRequest,
                operatorName,
                null,
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse unarchiveCase(Long id, String operatorName, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        requireStatus(record, STATUS_ARCHIVED, "Only ARCHIVED case can be unarchived");

        CaseProcess archiveProcess = caseMapper.findLatestArchiveProcessByCaseId(id);
        String restoreStatus = archiveProcess == null || archiveProcess.getFromStatus() == null
                ? STATUS_EXECUTED
                : archiveProcess.getFromStatus();

        caseMapper.updateCaseStatus(id, restoreStatus, record.getAcceptanceTime());
        addProcess(id, STATUS_ARCHIVED, restoreStatus, operatorName, "Case unarchived", httpRequest);
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

    private CaseEvidence requireEvidence(Long caseId, Long evidenceId) {
        CaseEvidence evidence = caseMapper.findEvidenceByIdAndCaseId(caseId, evidenceId);
        if (evidence == null) {
            throw new AuthException(404, "Evidence not found");
        }
        return evidence;
    }

    private void requireStatus(CaseRecord record, String expectedStatus, String message) {
        if (!expectedStatus.equals(record.getStatus())) {
            throw new AuthException(400, message);
        }
    }

    private PageResult<CaseListItem> toPageResult(List<CaseRecord> records, long total, int page, int size) {
        List<CaseListItem> items = new ArrayList<>(records.size());
        for (CaseRecord record : records) {
            items.add(toListItem(record));
        }
        return new PageResult<>(items, total, page, size);
    }

    private String buildDossierContent(CaseRecord record,
                                       List<CaseProcess> processes,
                                       List<CaseEvidence> evidences) {
        return "Case Number: " + record.getCaseNumber() + "\n"
                + "Title: " + record.getTitle() + "\n"
                + "Status: " + record.getStatus() + "\n"
                + "Type: " + record.getTypeCode() + "\n"
                + "DepartmentId: " + record.getDepartmentId() + "\n"
                + "Reporter: " + record.getReporterName() + "\n"
                + "Incident Time: " + record.getIncidentTime() + "\n"
                + "Deadline Time: " + record.getDeadlineTime() + "\n"
                + "Processes: " + processes.size() + "\n"
                + "Evidences: " + evidences.size() + "\n"
                + "Generated At: " + LocalDateTime.now() + "\n";
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

