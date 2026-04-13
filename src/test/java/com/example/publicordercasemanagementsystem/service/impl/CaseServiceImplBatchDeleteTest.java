package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.CaseMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.CaseRecord;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.CaseWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceImplBatchDeleteTest {

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CaseWorkflowService caseWorkflowService;

    private CaseServiceImpl caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseServiceImpl(caseMapper, userMapper, caseWorkflowService);
    }

    @Test
    void deleteCasesShouldAllowBatchDelete() {
        User operator = new User();
        operator.setId(1L);
        operator.setRole("police_officer");

        CaseRecord case1 = new CaseRecord();
        case1.setId(10L);
        CaseRecord case2 = new CaseRecord();
        case2.setId(11L);

        when(userMapper.findById(1L)).thenReturn(operator);
        when(caseMapper.findById(10L)).thenReturn(case1);
        when(caseMapper.findById(11L)).thenReturn(case2);

        caseService.deleteCases(List.of(10L, 11L, 11L), 1L);

        verify(caseMapper).deleteByIds(List.of(10L, 11L));
    }

    @Test
    void deleteCasesShouldRejectMissingIds() {
        User operator = new User();
        operator.setId(1L);
        operator.setRole("police_officer");
        when(userMapper.findById(1L)).thenReturn(operator);

        AuthException ex = assertThrows(AuthException.class,
                () -> caseService.deleteCases(List.of(), 1L));

        assertEquals(400, ex.getStatus());
        verify(caseMapper, never()).deleteByIds(any());
    }
}

