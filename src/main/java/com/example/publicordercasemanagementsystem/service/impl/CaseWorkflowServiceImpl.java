package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.PendingWorkflowTaskItem;
import com.example.publicordercasemanagementsystem.dto.StartCaseWorkflowRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowActionRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowInstanceResponse;
import com.example.publicordercasemanagementsystem.dto.WorkflowTaskItem;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.CaseMapper;
import com.example.publicordercasemanagementsystem.mapper.CaseWorkflowMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowActionLog;
import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowInstance;
import com.example.publicordercasemanagementsystem.pojo.CaseWorkflowTask;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.pojo.WorkflowDefinition;
import com.example.publicordercasemanagementsystem.pojo.WorkflowNode;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import com.example.publicordercasemanagementsystem.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseWorkflowServiceImpl implements CaseWorkflowService {

    private static final String ADMIN_ROLE = "admin";
    private static final String POLICE_ROLE = "police_officer";
    private static final String SUPERVISOR_ROLE = "supervisor";

    private static final String INSTANCE_PENDING = "PENDING";
    private static final String INSTANCE_APPROVED = "APPROVED";
    private static final String INSTANCE_REJECTED = "REJECTED";

    private static final String TASK_TODO = "TODO";
    private static final String TASK_APPROVED = "APPROVED";
    private static final String TASK_REJECTED = "REJECTED";
    private static final String TASK_CANCELLED = "CANCELLED";

    private final CaseWorkflowMapper workflowMapper;
    private final CaseMapper caseMapper;
    private final UserMapper userMapper;

    public CaseWorkflowServiceImpl(CaseWorkflowMapper workflowMapper,
                                   CaseMapper caseMapper,
                                   UserMapper userMapper) {
        this.workflowMapper = workflowMapper;
        this.caseMapper = caseMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse startCaseWorkflow(Long caseId,
                                                      String flowType,
                                                      StartCaseWorkflowRequest request,
                                                      String operatorName,
                                                      String idempotencyKey,
                                                      HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        validateIdempotency(idempotencyKey);

        String normalizedFlowType = normalizeFlowType(flowType);
        CaseRecord record = requireCase(caseId);
        validateStarterRole(normalizedFlowType, operator);
        validateCaseStatusForStart(normalizedFlowType, record.getStatus());

        WorkflowDefinition definition = requireDefinition(normalizedFlowType);
        List<WorkflowNode> nodes = workflowMapper.findNodesByDefinitionId(definition.getId());
        if (nodes.isEmpty()) {
            throw new AuthException(500, "Workflow nodes are not configured");
        }
        if (workflowMapper.findActiveInstanceByCaseIdAndFlowType(caseId, normalizedFlowType) != null) {
            throw new AuthException(409, "Workflow already started and pending");
        }

        WorkflowNode firstNode = nodes.get(0);
        LocalDateTime now = LocalDateTime.now();

        CaseWorkflowInstance instance = new CaseWorkflowInstance();
        instance.setCaseId(caseId);
        instance.setFlowType(normalizedFlowType);
        instance.setFlowVersion(definition.getVersion());
        instance.setStatus(INSTANCE_PENDING);
        instance.setCurrentNodeKey(firstNode.getNodeKey());
        instance.setStartedBy(operator.getId());
        instance.setStartedAt(now);
        instance.setSnapshotJson(null);
        workflowMapper.insertWorkflowInstance(instance);

        CaseWorkflowTask firstTask = new CaseWorkflowTask();
        firstTask.setInstanceId(instance.getId());
        firstTask.setNodeKey(firstNode.getNodeKey());
        firstTask.setRoundNo(1);
        firstTask.setAssigneeRole(firstNode.getRoleCode());
        firstTask.setAssigneeUserId(null);
        firstTask.setStatus(TASK_TODO);
        workflowMapper.insertWorkflowTask(firstTask);

        workflowMapper.insertActionLog(buildActionLog(instance.getId(),
                null,
                "START",
                operator,
                request == null ? null : request.getComment(),
                idempotencyKey,
                httpRequest));

        updateCaseStatus(caseId, normalizedFlowType, true, record);
        return getInstance(instance.getId());
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse approveTask(Long taskId,
                                                WorkflowActionRequest request,
                                                String operatorName,
                                                String idempotencyKey,
                                                HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        validateIdempotency(idempotencyKey);

        CaseWorkflowTask task = requireTodoTask(taskId);
        CaseWorkflowInstance instance = requireInstance(task.getInstanceId());
        ensureTaskPermission(task, operator);

        WorkflowDefinition definition = requireDefinition(instance.getFlowType());
        List<WorkflowNode> nodes = workflowMapper.findNodesByDefinitionId(definition.getId());
        Map<String, Integer> nodeOrderMap = toNodeOrderMap(nodes);

        LocalDateTime now = LocalDateTime.now();
        int updated = workflowMapper.updateTaskDecision(task.getId(),
                TASK_APPROVED,
                request == null ? null : request.getComment(),
                operator.getId(),
                now);
        if (updated <= 0) {
            throw new AuthException(409, "Task is already handled");
        }

        WorkflowNode nextNode = findNextNode(nodes, nodeOrderMap.get(task.getNodeKey()));
        if (nextNode == null) {
            workflowMapper.updateInstanceProgress(instance.getId(), INSTANCE_APPROVED, null, operator.getId(), now);
            CaseRecord record = requireCase(instance.getCaseId());
            updateCaseStatus(instance.getCaseId(), instance.getFlowType(), false, record);
        } else {
            workflowMapper.updateInstanceProgress(instance.getId(), INSTANCE_PENDING, nextNode.getNodeKey(), null, null);
            CaseWorkflowTask nextTask = new CaseWorkflowTask();
            nextTask.setInstanceId(instance.getId());
            nextTask.setNodeKey(nextNode.getNodeKey());
            nextTask.setRoundNo(task.getRoundNo() == null ? 1 : task.getRoundNo() + 1);
            nextTask.setAssigneeRole(nextNode.getRoleCode());
            nextTask.setStatus(TASK_TODO);
            workflowMapper.insertWorkflowTask(nextTask);
        }

        workflowMapper.insertActionLog(buildActionLog(instance.getId(),
                task.getId(),
                "APPROVE",
                operator,
                request == null ? null : request.getComment(),
                idempotencyKey,
                httpRequest));

        return getInstance(instance.getId());
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse approveCaseWorkflow(Long caseId,
                                                        String flowType,
                                                        WorkflowActionRequest request,
                                                        String operatorName,
                                                        String idempotencyKey,
                                                        HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        String normalizedFlowType = normalizeFlowType(flowType);
        CaseWorkflowTask task = ADMIN_ROLE.equals(operator.getRole())
                ? workflowMapper.findTodoTaskByCaseFlow(caseId, normalizedFlowType)
                : workflowMapper.findTodoTaskByCaseFlowAndRole(caseId, normalizedFlowType, operator.getRole());
        if (task == null) {
            throw new AuthException(404, "No pending task for current operator and flow");
        }
        return approveTask(task.getId(), request, operatorName, idempotencyKey, httpRequest);
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse rejectTask(Long taskId,
                                               WorkflowActionRequest request,
                                               String operatorName,
                                               String idempotencyKey,
                                               HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        validateIdempotency(idempotencyKey);

        CaseWorkflowTask task = requireTodoTask(taskId);
        CaseWorkflowInstance instance = requireInstance(task.getInstanceId());
        ensureTaskPermission(task, operator);

        if (request == null || request.getComment() == null || request.getComment().isBlank()) {
            throw new AuthException(400, "Reject comment is required");
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = workflowMapper.updateTaskDecision(task.getId(),
                TASK_REJECTED,
                request.getComment(),
                operator.getId(),
                now);
        if (updated <= 0) {
            throw new AuthException(409, "Task is already handled");
        }

        workflowMapper.closeTodoTasksByInstanceId(instance.getId(), TASK_CANCELLED, "Cancelled by rejection", operator.getId(), now);
        workflowMapper.updateInstanceProgress(instance.getId(), INSTANCE_REJECTED, null, operator.getId(), now);

        CaseRecord record = requireCase(instance.getCaseId());
        caseMapper.updateCaseStatus(record.getId(), rejectTargetStatus(instance.getFlowType()), record.getAcceptanceTime());

        workflowMapper.insertActionLog(buildActionLog(instance.getId(),
                task.getId(),
                "REJECT",
                operator,
                request.getComment(),
                idempotencyKey,
                httpRequest));

        return getInstance(instance.getId());
    }

    @Override
    @Transactional
    public WorkflowInstanceResponse rejectCaseWorkflow(Long caseId,
                                                       String flowType,
                                                       WorkflowActionRequest request,
                                                       String operatorName,
                                                       String idempotencyKey,
                                                       HttpServletRequest httpRequest) {
        User operator = requireOperator(operatorName);
        String normalizedFlowType = normalizeFlowType(flowType);
        CaseWorkflowTask task = ADMIN_ROLE.equals(operator.getRole())
                ? workflowMapper.findTodoTaskByCaseFlow(caseId, normalizedFlowType)
                : workflowMapper.findTodoTaskByCaseFlowAndRole(caseId, normalizedFlowType, operator.getRole());
        if (task == null) {
            throw new AuthException(404, "No pending task for current operator and flow");
        }
        return rejectTask(task.getId(), request, operatorName, idempotencyKey, httpRequest);
    }

    @Override
    public WorkflowInstanceResponse getInstance(Long instanceId) {
        CaseWorkflowInstance instance = requireInstance(instanceId);
        List<CaseWorkflowTask> tasks = workflowMapper.findTasksByInstanceId(instanceId);

        WorkflowInstanceResponse response = new WorkflowInstanceResponse();
        response.setId(instance.getId());
        response.setCaseId(instance.getCaseId());
        response.setFlowType(instance.getFlowType());
        response.setStatus(instance.getStatus());
        response.setCurrentNodeKey(instance.getCurrentNodeKey());
        response.setStartedBy(instance.getStartedBy());
        response.setStartedByName(instance.getStartedByName());
        response.setStartedAt(instance.getStartedAt());
        response.setFinishedBy(instance.getFinishedBy());
        response.setFinishedByName(instance.getFinishedByName());
        response.setFinishedAt(instance.getFinishedAt());

        List<WorkflowTaskItem> taskItems = new ArrayList<>(tasks.size());
        for (CaseWorkflowTask task : tasks) {
            WorkflowTaskItem item = new WorkflowTaskItem();
            item.setId(task.getId());
            item.setInstanceId(task.getInstanceId());
            item.setNodeKey(task.getNodeKey());
            item.setNodeName(task.getNodeName());
            item.setAssigneeRole(task.getAssigneeRole());
            item.setStatus(task.getStatus());
            item.setComment(task.getComment());
            item.setActedBy(task.getActedBy());
            item.setActedByName(task.getActedByName());
            item.setActedAt(task.getActedAt());
            item.setCreatedAt(task.getCreatedAt());
            taskItems.add(item);
        }
        response.setTasks(taskItems);
        return response;
    }

    @Override
    public List<WorkflowInstanceResponse> listCaseWorkflows(Long caseId) {
        requireCase(caseId);
        List<CaseWorkflowInstance> instances = workflowMapper.findByCaseId(caseId);
        List<WorkflowInstanceResponse> responses = new ArrayList<>(instances.size());
        for (CaseWorkflowInstance instance : instances) {
            responses.add(getInstance(instance.getId()));
        }
        return responses;
    }

    @Override
    public List<PendingWorkflowTaskItem> listPendingTasks(String operatorName) {
        User operator = requireOperator(operatorName);
        List<CaseWorkflowTask> tasks = workflowMapper.findPendingTasksByRole(operator.getRole());
        List<PendingWorkflowTaskItem> items = new ArrayList<>(tasks.size());
        for (CaseWorkflowTask task : tasks) {
            PendingWorkflowTaskItem item = new PendingWorkflowTaskItem();
            item.setTaskId(task.getId());
            item.setInstanceId(task.getInstanceId());
            item.setCaseId(task.getCaseId());
            item.setCaseNumber(task.getCaseNumber());
            item.setFlowType(task.getFlowType());
            item.setNodeKey(task.getNodeKey());
            item.setNodeName(task.getNodeName());
            item.setCreatedAt(task.getCreatedAt());
            items.add(item);
        }
        return items;
    }

    private WorkflowNode findNextNode(List<WorkflowNode> nodes, Integer currentOrder) {
        if (currentOrder == null) {
            return null;
        }
        for (WorkflowNode node : nodes) {
            if (node.getNodeOrder() != null && node.getNodeOrder() > currentOrder) {
                return node;
            }
        }
        return null;
    }

    private Map<String, Integer> toNodeOrderMap(List<WorkflowNode> nodes) {
        Map<String, Integer> map = new HashMap<>();
        for (WorkflowNode node : nodes) {
            map.put(node.getNodeKey(), node.getNodeOrder());
        }
        return map;
    }

    private void ensureTaskPermission(CaseWorkflowTask task, User operator) {
        if (ADMIN_ROLE.equals(operator.getRole())) {
            return;
        }
        if (!task.getAssigneeRole().equals(operator.getRole())) {
            throw new AuthException(403, "Current role cannot process this task");
        }
    }

    private void validateStarterRole(String flowType, User operator) {
        if (ADMIN_ROLE.equals(operator.getRole())) {
            return;
        }
        if ("ACCEPTANCE_REVIEW".equals(flowType)) {
            if (!POLICE_ROLE.equals(operator.getRole()) && !SUPERVISOR_ROLE.equals(operator.getRole())) {
                throw new AuthException(403, "Only police officer or supervisor can start acceptance review");
            }
            return;
        }
        if ("FILING_REVIEW".equals(flowType)
                || "DECISION_REVIEW".equals(flowType) || "EXECUTION_REVIEW".equals(flowType)
                || "ARCHIVE_REVIEW".equals(flowType)) {
            if (!POLICE_ROLE.equals(operator.getRole())) {
                throw new AuthException(403, "Only police officer can start this workflow");
            }
            return;
        }
        if ("LEGAL_AUDIT_REVIEW".equals(flowType) && !POLICE_ROLE.equals(operator.getRole())) {
            throw new AuthException(403, "Only police officer can submit legal audit review");
        }
    }

    private void validateCaseStatusForStart(String flowType, String caseStatus) {
        List<String> expected;
        if ("ACCEPTANCE_REVIEW".equals(flowType)) {
            expected = List.of("REGISTERED");
        } else if ("FILING_REVIEW".equals(flowType)) {
            expected = List.of("ACCEPTED");
        } else if ("LEGAL_AUDIT_REVIEW".equals(flowType)) {
            expected = List.of("INVESTIGATING");
        } else if ("DECISION_REVIEW".equals(flowType)) {
            expected = List.of("LEGAL_AUDIT_PASSED", "LEGAL_REVIEW");
        } else if ("EXECUTION_REVIEW".equals(flowType)) {
            expected = List.of("DECIDED");
        } else if ("ARCHIVE_REVIEW".equals(flowType)) {
            expected = List.of("EXECUTED");
        } else {
            throw new AuthException(400, "Unsupported flow type");
        }
        if (!expected.contains(caseStatus)) {
            throw new AuthException(400, "Case status does not match workflow requirement: expected " + expected);
        }
    }

    private void updateCaseStatus(Long caseId, String flowType, boolean starting, CaseRecord record) {
        String status;
        if (starting) {
            status = flowType;
        } else if ("ACCEPTANCE_REVIEW".equals(flowType)) {
            status = "ACCEPTED";
        } else if ("FILING_REVIEW".equals(flowType)) {
            status = "FILED";
        } else if ("LEGAL_AUDIT_REVIEW".equals(flowType)) {
            status = "LEGAL_AUDIT_PASSED";
        } else if ("DECISION_REVIEW".equals(flowType)) {
            status = "DECIDED";
        } else if ("EXECUTION_REVIEW".equals(flowType)) {
            status = "EXECUTED";
        } else if ("ARCHIVE_REVIEW".equals(flowType)) {
            status = "ARCHIVED";
        } else {
            throw new AuthException(400, "Unsupported flow type");
        }
        caseMapper.updateCaseStatus(caseId, status, record.getAcceptanceTime());
    }

    private String rejectTargetStatus(String flowType) {
        if ("ACCEPTANCE_REVIEW".equals(flowType)) {
            return "REGISTERED";
        }
        if ("FILING_REVIEW".equals(flowType)) {
            return "ACCEPTED";
        }
        if ("LEGAL_AUDIT_REVIEW".equals(flowType)) {
            return "INVESTIGATING";
        }
        if ("DECISION_REVIEW".equals(flowType)) {
            return "LEGAL_AUDIT_PASSED";
        }
        if ("EXECUTION_REVIEW".equals(flowType)) {
            return "DECIDED";
        }
        if ("ARCHIVE_REVIEW".equals(flowType)) {
            return "EXECUTED";
        }
        throw new AuthException(400, "Unsupported flow type");
    }

    private WorkflowDefinition requireDefinition(String flowType) {
        WorkflowDefinition definition = workflowMapper.findActiveDefinitionByFlowType(flowType);
        if (definition == null) {
            throw new AuthException(400, "Workflow definition is not configured for flow type: " + flowType);
        }
        return definition;
    }

    private String normalizeFlowType(String flowType) {
        if (flowType == null || flowType.isBlank()) {
            throw new AuthException(400, "flowType is required");
        }
        return flowType.trim().toUpperCase();
    }

    private User requireOperator(String operatorName) {
        if (operatorName == null || operatorName.isBlank()) {
            throw new AuthException(401, "Unauthenticated");
        }
        User user = userMapper.findByName(operatorName);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthException(401, "Operator not found or inactive");
        }
        return user;
    }

    private CaseRecord requireCase(Long caseId) {
        CaseRecord record = caseMapper.findById(caseId);
        if (record == null) {
            throw new AuthException(404, "Case not found");
        }
        return record;
    }

    private CaseWorkflowTask requireTodoTask(Long taskId) {
        CaseWorkflowTask task = workflowMapper.findTodoTaskById(taskId);
        if (task == null) {
            throw new AuthException(404, "Task not found or already handled");
        }
        return task;
    }

    private CaseWorkflowInstance requireInstance(Long instanceId) {
        CaseWorkflowInstance instance = workflowMapper.findByInstanceId(instanceId);
        if (instance == null) {
            throw new AuthException(404, "Workflow instance not found");
        }
        return instance;
    }

    private void validateIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        if (workflowMapper.findActionLogByRequestId(idempotencyKey) != null) {
            throw new AuthException(409, "Duplicate request");
        }
    }

    private CaseWorkflowActionLog buildActionLog(Long instanceId,
                                                 Long taskId,
                                                 String actionType,
                                                 User operator,
                                                 String comment,
                                                 String requestId,
                                                 HttpServletRequest httpRequest) {
        CaseWorkflowActionLog actionLog = new CaseWorkflowActionLog();
        actionLog.setInstanceId(instanceId);
        actionLog.setTaskId(taskId);
        actionLog.setActionType(actionType);
        actionLog.setActorId(operator.getId());
        actionLog.setActorName(operator.getName());
        actionLog.setActorRole(operator.getRole());
        actionLog.setComment(comment);
        actionLog.setPayloadJson(null);
        actionLog.setRequestId(requestId);
        actionLog.setIp(httpRequest == null ? null : RequestUtil.getClientIp(httpRequest));
        actionLog.setUserAgent(httpRequest == null ? null : httpRequest.getHeader("User-Agent"));
        actionLog.setCreatedAt(LocalDateTime.now());
        return actionLog;
    }
}

