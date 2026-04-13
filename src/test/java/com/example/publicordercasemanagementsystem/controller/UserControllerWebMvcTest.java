package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.dto.UserListItem;
import com.example.publicordercasemanagementsystem.exception.ApiExceptionHandler;
import com.example.publicordercasemanagementsystem.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsersShouldReturnApiResponseEnvelope() throws Exception {
        UserListItem item = new UserListItem();
        item.setId(1L);
        item.setName("alice");
        item.setRole("ADMIN");

        PageResult<UserListItem> pageResult = new PageResult<>(List.of(item), 1, 1, 10);
        when(userService.listUsers(any(), any(), any(), any(), any(), any(), any())).thenReturn(pageResult);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("alice"));
    }

    @Test
    void getCurrentUserShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    void getCurrentUserShouldReturnCurrentUserInfo() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setName("alice");
        userInfo.setRole("ADMIN");

        when(userService.getUserInfoById(eq(1L))).thenReturn(userInfo);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("alice"));
    }

    @Test
    void updateUserRoleShouldReturnUpdatedUser() throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(7L);
        userInfo.setName("bob");
        userInfo.setRole("supervisor");
        when(userService.updateUserRole(eq(7L), any(), eq(1L))).thenReturn(userInfo);

        mockMvc.perform(put("/api/users/7/role")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"role\":\"supervisor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User role updated successfully"))
                .andExpect(jsonPath("$.data.role").value("supervisor"));
    }

    @Test
    void changePasswordShouldReturnSuccessMessage() throws Exception {
        doNothing().when(userService).changeUserPassword(eq(9L), any(), eq(1L));

        mockMvc.perform(put("/api/users/9/password")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"password\":\"NewPass@123\",\"confirmPassword\":\"NewPass@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User password updated successfully"));
    }

    @Test
    void deleteUserShouldReturnSuccessMessage() throws Exception {
        doNothing().when(userService).deleteUser(eq(9L), eq(1L));

        mockMvc.perform(delete("/api/users/9").with(authenticatedUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void deleteUsersShouldReturnSuccessMessage() throws Exception {
        doNothing().when(userService).deleteUsers(any(), eq(1L));

        mockMvc.perform(delete("/api/users/batch")
                        .with(authenticatedUser(1L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"ids\":[9,10]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users deleted successfully"));
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
