package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.CaseDetailResponse;
import com.example.publicordercasemanagementsystem.dto.LegalReviewSubmitRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.CaseMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceImplWorkflowCompatTest {

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CaseWorkflowService caseWorkflowService;

    @InjectMocks
    private CaseServiceImpl caseService;

    private User supervisor;

    @BeforeEach
    void setUp() {
        supervisor = new User();
        supervisor.setId(5L);
        supervisor.setName("supervisor_chen");
        supervisor.setRole("supervisor");
    }

    @Test
    void acceptCaseShouldStartAndApproveWorkflowWithIdempotency() {
        CaseRecord registered = new CaseRecord();
        registered.setId(101L);
        registered.setStatus("REGISTERED");

        CaseRecord accepted = new CaseRecord();
        accepted.setId(101L);
        accepted.setStatus("ACCEPTED");

        when(caseMapper.findById(101L)).thenReturn(registered, accepted);
        when(userMapper.findByName("supervisor_chen")).thenReturn(supervisor);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Idempotency-Key", "acc-101");

        CaseDetailResponse response = caseService.acceptCase(101L, "supervisor_chen", request);

        verify(caseWorkflowService).startCaseWorkflow(eq(101L), eq("ACCEPTANCE_REVIEW"), any(),
                eq("supervisor_chen"), eq("acc-101"), eq(request));
        verify(caseWorkflowService).approveCaseWorkflow(eq(101L), eq("ACCEPTANCE_REVIEW"), any(),
                eq("supervisor_chen"), eq("acc-101-approve"), eq(request));
        assertEquals("ACCEPTED", response.getStatus());
    }

    @Test
    void submitLegalReviewShouldPassIdempotencyKeyToWorkflowService() {
        CaseRecord record = new CaseRecord();
        record.setId(101L);
        record.setStatus("LEGAL_AUDIT_REVIEW");
        when(caseMapper.findById(101L)).thenReturn(record);

        LegalReviewSubmitRequest requestBody = new LegalReviewSubmitRequest();
        requestBody.setComment("submit legal review");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Idempotency-Key", "lr-101");

        caseService.submitLegalReview(101L, requestBody, "officer_wang", request);

        verify(caseWorkflowService).startCaseWorkflow(eq(101L), eq("LEGAL_AUDIT_REVIEW"), any(),
                eq("officer_wang"), eq("lr-101"), eq(request));
    }

    @Test
    void transitionStatusShouldRejectIllegalTransition() {
        CaseRecord record = new CaseRecord();
        record.setId(101L);
        record.setStatus("REGISTERED");
        when(caseMapper.findById(101L)).thenReturn(record);

        assertThrows(AuthException.class, () -> caseService.transitionStatus(101L,
                buildTransition("DECIDED"), "officer_wang", new MockHttpServletRequest()));
    }

    private com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest buildTransition(String toStatus) {
        com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest request =
                new com.example.publicordercasemanagementsystem.dto.StatusTransitionRequest();
        request.setToStatus(toStatus);
        request.setComment("test");
        return request;
    }
}

