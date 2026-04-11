package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.PendingWorkflowTaskItem;
import com.example.publicordercasemanagementsystem.dto.StartCaseWorkflowRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowActionRequest;
import com.example.publicordercasemanagementsystem.dto.WorkflowInstanceResponse;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CaseWorkflowControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private CaseWorkflowService caseWorkflowService;

    @InjectMocks
    private CaseWorkflowController caseWorkflowController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(caseWorkflowController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void startCaseWorkflowShouldReturnOk() throws Exception {
        WorkflowInstanceResponse response = new WorkflowInstanceResponse();
        response.setId(1L);
        response.setCaseId(101L);
        response.setFlowType("FILING_REVIEW");
        response.setStatus("PENDING");

        when(caseWorkflowService.startCaseWorkflow(eq(101L), eq("FILING_REVIEW"), any(StartCaseWorkflowRequest.class),
                eq(1L), eq("req-1"), any())).thenReturn(response);

        mockMvc.perform(post("/api/cases/101/workflows/FILING_REVIEW/start")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "req-1")
                        .content("{\"comment\":\"submit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Workflow started successfully"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void listPendingTasksShouldReturnOk() throws Exception {
        PendingWorkflowTaskItem item = new PendingWorkflowTaskItem();
        item.setTaskId(11L);
        item.setFlowType("FILING_REVIEW");
        when(caseWorkflowService.listPendingTasks(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/workflows/tasks/my-pending").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskId").value(11));
    }

    @Test
    void approveTaskShouldReturnOk() throws Exception {
        WorkflowInstanceResponse response = new WorkflowInstanceResponse();
        response.setId(1L);
        response.setStatus("APPROVED");
        when(caseWorkflowService.approveTask(eq(11L), any(WorkflowActionRequest.class),
                eq(1L), eq("req-2"), any())).thenReturn(response);

        mockMvc.perform(post("/api/workflows/tasks/11/approve")
                        .with(authenticatedUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "req-2")
                        .content("{\"comment\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Task approved successfully"));
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, List.of())
            );
            return request;
        };
    }
}

