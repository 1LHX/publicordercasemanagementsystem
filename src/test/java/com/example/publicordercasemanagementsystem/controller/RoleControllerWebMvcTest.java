package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.RoleItem;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.RoleService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoleControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listRolesShouldReturnEnvelope() throws Exception {
        RoleItem item = new RoleItem();
        item.setCode("admin");
        item.setName("Administrator");
        when(roleService.listRoles(null)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("admin"));
    }

    @Test
    void createRoleShouldReturnCreatedMessage() throws Exception {
        RoleItem item = new RoleItem();
        item.setCode("auditor");
        item.setName("Auditor");
        when(roleService.createRole(any(), eq(1L))).thenReturn(item);

        mockMvc.perform(post("/api/roles")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"code\":\"auditor\",\"name\":\"Auditor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role created successfully"))
                .andExpect(jsonPath("$.data.code").value("auditor"));
    }

    @Test
    void createRoleShouldReturn403WhenServiceDeniesPermission() throws Exception {
        when(roleService.createRole(any(), eq(2L)))
                .thenThrow(new AuthException(403, "当前角色无此操作权限。"));

        mockMvc.perform(post("/api/roles")
                        .with(authenticatedUser(2L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"code\":\"auditor\",\"name\":\"Auditor\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("当前角色无此操作权限。"));
    }

    @Test
    void deleteRoleShouldReturnSuccessMessage() throws Exception {
        doNothing().when(roleService).deleteRole(eq("auditor"), eq(1L));

        mockMvc.perform(delete("/api/roles/auditor").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
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

