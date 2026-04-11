package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.PendingWorkflowTaskItem;
import com.example.publicordercasemanagementsystem.dto.StartCaseWorkflowRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowActionRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowInstanceResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CaseWorkflowService {

    WorkflowInstanceResponse startCaseWorkflow(Long caseId,
                                               String flowType,
                                               StartCaseWorkflowRequest request,
                                               Long operatorUserId,
                                               String idempotencyKey,
                                               HttpServletRequest httpRequest);

    WorkflowInstanceResponse approveTask(Long taskId,
                                         WorkflowActionRequest request,
                                          Long operatorUserId,
                                         String idempotencyKey,
                                         HttpServletRequest httpRequest);

    WorkflowInstanceResponse approveCaseWorkflow(Long caseId,
                                                 String flowType,
                                                 WorkflowActionRequest request,
                                                 Long operatorUserId,
                                                 String idempotencyKey,
                                                 HttpServletRequest httpRequest);

    WorkflowInstanceResponse rejectTask(Long taskId,
                                        WorkflowActionRequest request,
                                         Long operatorUserId,
                                        String idempotencyKey,
                                        HttpServletRequest httpRequest);

    WorkflowInstanceResponse rejectCaseWorkflow(Long caseId,
                                                String flowType,
                                                WorkflowActionRequest request,
                                                Long operatorUserId,
                                                String idempotencyKey,
                                                HttpServletRequest httpRequest);

    WorkflowInstanceResponse getInstance(Long instanceId);

    List<WorkflowInstanceResponse> listCaseWorkflows(Long caseId);

    List<PendingWorkflowTaskItem> listPendingTasks(Long operatorUserId);
}

