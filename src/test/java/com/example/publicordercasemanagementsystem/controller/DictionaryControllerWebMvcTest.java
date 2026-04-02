package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.CaseTypeItem;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.DictionaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DictionaryControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private DictionaryService dictionaryService;

    @InjectMocks
    private DictionaryController dictionaryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dictionaryController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void listCaseTypesShouldReturnApiResponseEnvelope() throws Exception {
        CaseTypeItem item = new CaseTypeItem();
        item.setCode("DISPUTE");
        item.setName("Dispute");
        when(dictionaryService.listCaseTypes()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/dictionaries/case-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("DISPUTE"));
    }

    @Test
    void listCaseTypesShouldReturnBusinessError() throws Exception {
        when(dictionaryService.listCaseTypes()).thenThrow(new AuthException(503, "Dictionary unavailable"));

        mockMvc.perform(get("/api/dictionaries/case-types"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value("Dictionary unavailable"));
    }
}

