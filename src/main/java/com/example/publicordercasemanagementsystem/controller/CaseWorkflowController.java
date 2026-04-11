package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.PendingWorkflowTaskItem;
import com.example.publicordercasemanagementsystem.dto.StartCaseWorkflowRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowActionRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowInstanceResponse;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CaseWorkflowController {

    private final CaseWorkflowService caseWorkflowService;

    public CaseWorkflowController(CaseWorkflowService caseWorkflowService) {
        this.caseWorkflowService = caseWorkflowService;
    }

    @PostMapping("/cases/{id}/workflows/{flowType}/start")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startCaseWorkflow(@PathVariable("id") Long caseId,
                                                                                   @PathVariable String flowType,
                                                                                   @Valid @RequestBody(required = false) StartCaseWorkflowRequest request,
                                                                                   @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                                                   HttpServletRequest httpRequest) {
        WorkflowInstanceResponse response = caseWorkflowService.startCaseWorkflow(
                caseId, flowType, request, getCurrentUserId(), idempotencyKey, httpRequest
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Workflow started successfully"));
    }

    @GetMapping("/cases/{id}/workflows")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> listCaseWorkflows(@PathVariable("id") Long caseId) {
        return ResponseEntity.ok(ApiResponse.ok(caseWorkflowService.listCaseWorkflows(caseId)));
    }

    @GetMapping("/workflows/instances/{instanceId}")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getWorkflowInstance(@PathVariable Long instanceId) {
        return ResponseEntity.ok(ApiResponse.ok(caseWorkflowService.getInstance(instanceId)));
    }

    @GetMapping("/workflows/tasks/my-pending")
    public ResponseEntity<ApiResponse<List<PendingWorkflowTaskItem>>> listMyPendingTasks() {
        return ResponseEntity.ok(ApiResponse.ok(caseWorkflowService.listPendingTasks(getCurrentUserId())));
    }

    @PostMapping("/workflows/tasks/{taskId}/approve")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> approveTask(@PathVariable Long taskId,
                                                                             @Valid @RequestBody(required = false) WorkflowActionRequest request,
                                                                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                                             HttpServletRequest httpRequest) {
        WorkflowInstanceResponse response = caseWorkflowService.approveTask(
                taskId, request, getCurrentUserId(), idempotencyKey, httpRequest
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Task approved successfully"));
    }

    @PostMapping("/workflows/tasks/{taskId}/reject")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> rejectTask(@PathVariable Long taskId,
                                                                            @Valid @RequestBody WorkflowActionRequest request,
                                                                            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                                            HttpServletRequest httpRequest) {
        WorkflowInstanceResponse response = caseWorkflowService.rejectTask(
                taskId, request, getCurrentUserId(), idempotencyKey, httpRequest
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Task rejected successfully"));
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

