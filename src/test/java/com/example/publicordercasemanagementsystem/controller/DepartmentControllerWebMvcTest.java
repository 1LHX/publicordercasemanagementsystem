package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.DepartmentItem;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.DepartmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listDepartmentsShouldReturnEnvelope() throws Exception {
        DepartmentItem item = new DepartmentItem();
        item.setId(1L);
        item.setName("Public Order Brigade");
        when(departmentService.listDepartments(null, null, null)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Public Order Brigade"));
    }

    @Test
    void createDepartmentShouldReturnSuccessMessage() throws Exception {
        DepartmentItem item = new DepartmentItem();
        item.setId(9L);
        item.setName("Traffic Patrol Unit");
        when(departmentService.createDepartment(any(), eq(1L))).thenReturn(item);

        mockMvc.perform(post("/api/departments")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Traffic Patrol Unit\",\"parentId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Department created successfully"))
                .andExpect(jsonPath("$.data.name").value("Traffic Patrol Unit"));
    }

    @Test
    void createDepartmentShouldReturn403WhenUnauthorized() throws Exception {
        when(departmentService.createDepartment(any(), eq(2L)))
                .thenThrow(new AuthException(403, "当前角色无此操作权限。"));

        mockMvc.perform(post("/api/departments")
                        .with(authenticatedUser(2L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Traffic Patrol Unit\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("当前角色无此操作权限。"));
    }

    @Test
    void deleteDepartmentShouldReturnSuccessMessage() throws Exception {
        doNothing().when(departmentService).deleteDepartment(eq(9L), eq(1L));

        mockMvc.perform(delete("/api/departments/9").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Department deleted successfully"));
    }

    @Test
    void updateDepartmentStatusShouldReturnSuccessMessage() throws Exception {
        DepartmentItem item = new DepartmentItem();
        item.setId(9L);
        item.setIsActive(false);
        when(departmentService.updateDepartmentStatus(eq(9L), any(), eq(1L))).thenReturn(item);

        mockMvc.perform(put("/api/departments/9/status")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Department status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
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

