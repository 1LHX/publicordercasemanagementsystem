package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.CreateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.RoleItem;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleStatusRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleItem>>> listRoles(@RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.listRoles(isActive)));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<RoleItem>> getRoleByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.getRoleByCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleItem>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleItem item = roleService.createRole(request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(item, "Role created successfully"));
    }

    @PutMapping("/{code}")
    public ResponseEntity<ApiResponse<RoleItem>> updateRole(@PathVariable String code,
                                                            @Valid @RequestBody UpdateRoleRequest request) {
        RoleItem item = roleService.updateRole(code, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(item, "Role updated successfully"));
    }

    @PutMapping("/{code}/status")
    public ResponseEntity<ApiResponse<RoleItem>> updateRoleStatus(@PathVariable String code,
                                                                  @Valid @RequestBody UpdateRoleStatusRequest request) {
        RoleItem item = roleService.updateRoleStatus(code, request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(item, "Role status updated successfully"));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String code) {
        roleService.deleteRole(code, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Role deleted successfully"));
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

