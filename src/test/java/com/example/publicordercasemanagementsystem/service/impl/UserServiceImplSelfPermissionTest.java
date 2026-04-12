package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.ChangePasswordRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserNameRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.RefreshTokenMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplSelfPermissionTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, refreshTokenMapper, passwordEncoder);
    }

    @Test
    void updateUserNameShouldAllowNonAdminToModifySelf() {
        User self = new User();
        self.setId(2L);
        self.setName("old_name");
        self.setRole("police_officer");

        User updated = new User();
        updated.setId(2L);
        updated.setName("new_name");
        updated.setRole("police_officer");

        UpdateUserNameRequest request = new UpdateUserNameRequest();
        request.setName("new_name");

        when(userMapper.findById(2L)).thenReturn(self, updated);
        when(userMapper.countByNameExcludeId("new_name", 2L)).thenReturn(0L);

        userService.updateUserName(2L, request, 2L);

        verify(userMapper).updateNameById(2L, "new_name");
        verify(refreshTokenMapper).revokeByUserId(2L);
    }

    @Test
    void updateUserNameShouldRejectNonAdminModifyingOthers() {
        User operator = new User();
        operator.setId(2L);
        operator.setRole("police_officer");

        UpdateUserNameRequest request = new UpdateUserNameRequest();
        request.setName("target_new_name");

        when(userMapper.findById(2L)).thenReturn(operator);

        AuthException ex = assertThrows(AuthException.class,
                () -> userService.updateUserName(3L, request, 2L));

        assertEquals(403, ex.getStatus());
        verify(userMapper, never()).updateNameById(eq(3L), anyString());
    }

    @Test
    void changePasswordShouldAllowAdminToModifyOthers() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole("admin");

        User target = new User();
        target.setId(3L);
        target.setRole("police_officer");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("NewPass@123");
        request.setConfirmPassword("NewPass@123");

        when(userMapper.findById(1L)).thenReturn(admin);
        when(userMapper.findById(3L)).thenReturn(target);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encoded-pass");

        userService.changeUserPassword(3L, request, 1L);

        verify(userMapper).updatePasswordById(3L, "encoded-pass");
        verify(refreshTokenMapper).revokeByUserId(3L);
    }
}

