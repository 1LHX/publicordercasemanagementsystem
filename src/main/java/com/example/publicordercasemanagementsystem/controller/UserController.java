package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.ChangePasswordRequest;
import com.example.publicordercasemanagementsystem.dto.PageResult;
import com.example.publicordercasemanagementsystem.dto.UpdateUserNameRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateUserStatusRequest;
import com.example.publicordercasemanagementsystem.dto.UserInfo;
import com.example.publicordercasemanagementsystem.dto.UserListItem;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<UserListItem>>> listUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<UserListItem> result = userService.listUsers(name, role, department, departmentId, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        UserInfo info = userService.getUserInfoById(getCurrentUserId());
        if (info == null) {
            throw new AuthException(401, "User not found");
        }
        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<ApiResponse<UserInfo>> updateUserName(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserNameRequest request) {
        UserInfo info = userService.updateUserName(id, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(info, "User name updated successfully"));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changeUserPassword(@PathVariable Long id,
                                                                @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeUserPassword(id, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "User password updated successfully"));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserInfo>> updateUserRole(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserRoleRequest request) {
        UserInfo info = userService.updateUserRole(id, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(info, "User role updated successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserInfo>> updateUserStatus(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        UserInfo info = userService.updateUserStatus(id, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(info, "User status updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "User deleted successfully"));
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
