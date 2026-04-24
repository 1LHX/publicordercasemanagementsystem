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
import com.example.publicordercasemanagementsystem.pojo.CaseProcess;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.CaseService;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import com.example.publicordercasemanagementsystem.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CaseServiceImpl implements CaseService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_INVESTIGATING = "INVESTIGATING";
    private static final String STATUS_LEGAL_REVIEW = "LEGAL_REVIEW";
    private static final String STATUS_LEGAL_AUDIT_PASSED = "LEGAL_AUDIT_PASSED";
    private static final String STATUS_DECIDED = "DECIDED";
    private static final String STATUS_EXECUTED = "EXECUTED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private static final String ROLE_SUPERVISOR = "supervisor";
    private static final String ROLE_ADMIN = "admin";

    private static final Map<String, Set<String>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            STATUS_REGISTERED, Set.of(STATUS_ACCEPTED, STATUS_ARCHIVED),
            STATUS_ACCEPTED, Set.of(STATUS_INVESTIGATING, STATUS_ARCHIVED),
            STATUS_INVESTIGATING, Set.of(STATUS_LEGAL_REVIEW, STATUS_ARCHIVED),
            STATUS_LEGAL_REVIEW, Set.of(STATUS_INVESTIGATING, STATUS_DECIDED, STATUS_ARCHIVED),
            STATUS_LEGAL_AUDIT_PASSED, Set.of(STATUS_DECIDED, STATUS_ARCHIVED),
            STATUS_DECIDED, Set.of(STATUS_EXECUTED, STATUS_ARCHIVED),
            STATUS_EXECUTED, Set.of(STATUS_ARCHIVED),
            STATUS_ARCHIVED, Set.of(STATUS_EXECUTED)
    );

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
    public CaseDetailResponse createCase(CreateCaseRequest request, Long operatorUserId, HttpServletRequest httpRequest) {
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
        record.setCreatorId(operatorUserId);
        caseMapper.insertCase(record);

        addProcess(record.getId(), null, STATUS_REGISTERED, operatorUserId, "Case created", httpRequest);
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
    public CaseDetailResponse updateCase(Long id, UpdateCaseRequest request, Long operatorUserId) {
        CaseRecord existing = requireCase(id);
        User currentUser = requireOperator(operatorUserId);
        if (STATUS_REGISTERED.equals(existing.getStatus())) {
            if (!Objects.equals(existing.getCreatorId(), currentUser.getId())) {
                throw new AuthException(403, "Only the creator can modify the case before acceptance");
            }
        } else if (STATUS_ACCEPTED.equals(existing.getStatus()) || STATUS_INVESTIGATING.equals(existing.getStatus())) {
            if (!Objects.equals(existing.getHandlingOfficerId(), currentUser.getId())) {
                throw new AuthException(403, "Only the assigned handling officer can modify the case after acceptance");
            }
        } else {
            throw new AuthException(403, "Case modification not allowed in current status");
        }
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
    public void deleteCase(Long id, Long operatorUserId) {
        requireOperator(operatorUserId);
        requireCase(id);
        caseMapper.deleteCase(id);
    }

    @Override
    @Transactional
    public void deleteCases(List<Long> ids, Long operatorUserId) {
        requireOperator(operatorUserId);
        List<Long> targetIds = normalizeIds(ids);

        for (Long id : targetIds) {
            requireCase(id);
        }

        caseMapper.deleteByIds(targetIds);
    }

    @Override
    public CaseDetailResponse acceptCase(Long id, Long operatorUserId, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        User operator = requireOperator(operatorUserId);
        if (!ROLE_SUPERVISOR.equals(operator.getRole()) && !ROLE_ADMIN.equals(operator.getRole())) {
            throw new AuthException(403, "Current role cannot accept case");
        }

        if (STATUS_ACCEPTED.equals(record.getStatus())) {
            return toDetail(record);
        }
        if (!STATUS_REGISTERED.equals(record.getStatus())) {
            throw new AuthException(400, "Only REGISTERED case can be accepted");
        }

        String idempotencyKey = extractIdempotencyKey(httpRequest);
        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment("Case accepted");
        caseWorkflowService.startCaseWorkflow(id,
                "ACCEPTANCE_REVIEW",
                workflowRequest,
                operatorUserId,
                idempotencyKey,
                httpRequest);

        WorkflowActionRequest actionRequest = new WorkflowActionRequest();
        actionRequest.setComment("Case accepted");
        caseWorkflowService.approveCaseWorkflow(id,
                "ACCEPTANCE_REVIEW",
                actionRequest,
                operatorUserId,
                idempotencyKey == null ? null : idempotencyKey + "-approve",
                httpRequest);

        addProcess(id, record.getStatus(), STATUS_ACCEPTED, operatorUserId, "Case accepted", httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse assignCase(Long id, AssignCaseRequest request, Long operatorUserId, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        User currentUser = requireOperator(operatorUserId);
        if (!ROLE_SUPERVISOR.equals(currentUser.getRole()) && !ROLE_ADMIN.equals(currentUser.getRole())) {
            throw new AuthException(403, "Only supervisors can reassign handling officers");
        }
        User officer = userMapper.findById(request.getHandlingOfficerId());
        if (officer == null || !Boolean.TRUE.equals(officer.getIsActive())) {
            throw new AuthException(400, "Handling officer not found or inactive");
        }
        caseMapper.assignOfficer(id, request.getHandlingOfficerId(), officer.getDepartmentId());
        addProcess(id, record.getStatus(), record.getStatus(), operatorUserId, "Assigned handling officer", httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse transitionStatus(Long id, StatusTransitionRequest request, Long operatorUserId, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        String fromStatus = record.getStatus();
        String toStatus = request.getToStatus() == null ? null : request.getToStatus().trim().toUpperCase();
        if (fromStatus != null && fromStatus.equals(toStatus)) {
            throw new AuthException(400, "Target status must be different from current status");
        }
        Set<String> allowedTargets = ALLOWED_STATUS_TRANSITIONS.getOrDefault(fromStatus, Set.of());
        if (!allowedTargets.contains(toStatus)) {
            throw new AuthException(400, "Illegal status transition");
        }
        LocalDateTime acceptanceTime = record.getAcceptanceTime();
        if (STATUS_ACCEPTED.equals(toStatus) && acceptanceTime == null) {
            acceptanceTime = LocalDateTime.now();
        }
        caseMapper.updateCaseStatus(id, toStatus, acceptanceTime);
        addProcess(id, fromStatus, toStatus, operatorUserId, request.getComment(), httpRequest);
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
    public CaseEvidenceItem addEvidence(Long id, CreateEvidenceRequest request, Long operatorUserId) {
        requireCase(id);
        User operator = requireOperator(operatorUserId);

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
    public CaseEvidenceItem updateEvidence(Long caseId, Long evidenceId, UpdateEvidenceRequest request, Long operatorUserId) {
        requireOperator(operatorUserId);
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
    public void deleteEvidence(Long caseId, Long evidenceId, Long operatorUserId) {
        requireOperator(operatorUserId);
        requireCase(caseId);
        requireEvidence(caseId, evidenceId);
        caseMapper.deleteEvidence(evidenceId);
    }

    @Override
    public CaseDetailResponse submitLegalReview(Long id,
                                                LegalReviewSubmitRequest request,
                                                Long operatorUserId,
                                                HttpServletRequest httpRequest) {
        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment(request.getComment());
        caseWorkflowService.startCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse approveLegalReview(Long id,
                                                 LegalReviewApproveRequest request,
                                                  Long operatorUserId,
                                                 HttpServletRequest httpRequest) {
        WorkflowActionRequest workflowRequest = new WorkflowActionRequest();
        workflowRequest.setComment(request.getComment());
        caseWorkflowService.approveCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse rejectLegalReview(Long id,
                                                LegalReviewRejectRequest request,
                                                Long operatorUserId,
                                                HttpServletRequest httpRequest) {
        WorkflowActionRequest workflowRequest = new WorkflowActionRequest();
        workflowRequest.setComment(request.getReason());
        caseWorkflowService.rejectCaseWorkflow(id,
                "LEGAL_AUDIT_REVIEW",
                workflowRequest,
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse saveDecision(Long id,
                                           SaveDecisionRequest request,
                                           Long operatorUserId,
                                           HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorUserId);
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
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse recordExecution(Long id,
                                              RecordExecutionRequest request,
                                              Long operatorUserId,
                                              HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorUserId);
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
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse archiveCase(Long id, Long operatorUserId, HttpServletRequest httpRequest) {
        StartCaseWorkflowRequest workflowRequest = new StartCaseWorkflowRequest();
        workflowRequest.setComment("Archive submitted for approval");
        caseWorkflowService.startCaseWorkflow(id,
                "ARCHIVE_REVIEW",
                workflowRequest,
                operatorUserId,
                extractIdempotencyKey(httpRequest),
                httpRequest);
        return toDetail(requireCase(id));
    }

    @Override
    public CaseDetailResponse unarchiveCase(Long id, Long operatorUserId, HttpServletRequest httpRequest) {
        CaseRecord record = requireCase(id);
        requireStatus(record, STATUS_ARCHIVED, "Only ARCHIVED case can be unarchived");

        CaseProcess archiveProcess = caseMapper.findLatestArchiveProcessByCaseId(id);
        String restoreStatus = archiveProcess == null || archiveProcess.getFromStatus() == null
                ? STATUS_EXECUTED
                : archiveProcess.getFromStatus();

        caseMapper.updateCaseStatus(id, restoreStatus, record.getAcceptanceTime());
        addProcess(id, STATUS_ARCHIVED, restoreStatus, operatorUserId, "Case unarchived", httpRequest);
        return toDetail(requireCase(id));
    }

    private void addProcess(Long caseId,
                            String fromStatus,
                            String toStatus,
                            Long operatorUserId,
                            String comment,
                            HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorUserId);
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

    private User requireOperator(Long operatorUserId) {
        if (operatorUserId == null) {
            throw new AuthException(401, "Unauthenticated");
        }
        User user = userMapper.findById(operatorUserId);
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

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new AuthException(400, "ids is required");
        }
        List<Long> uniqueIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (uniqueIds.isEmpty()) {
            throw new AuthException(400, "ids is required");
        }
        return uniqueIds;
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

    private String extractIdempotencyKey(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
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

