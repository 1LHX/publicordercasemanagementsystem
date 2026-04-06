package com.example.publicordercasemanagementsystem.controller;

import com.example.publicordercasemanagementsystem.dto.ApiResponse;
import com.example.publicordercasemanagementsystem.dto.CreateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.DepartmentItem;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentStatusRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.service.DepartmentService;
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
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentItem>>> listDepartments(@RequestParam(required = false) String name,
                                                                             @RequestParam(required = false) Boolean isActive,
                                                                             @RequestParam(required = false) Long parentId) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.listDepartments(name, isActive, parentId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentItem>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.getDepartmentById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentItem>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentItem item = departmentService.createDepartment(request, getCurrentUserName());
        return ResponseEntity.ok(ApiResponse.ok(item, "Department created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentItem>> updateDepartment(@PathVariable Long id,
                                                                        @Valid @RequestBody UpdateDepartmentRequest request) {
        DepartmentItem item = departmentService.updateDepartment(id, request, getCurrentUserName());
        return ResponseEntity.ok(ApiResponse.ok(item, "Department updated successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DepartmentItem>> updateDepartmentStatus(@PathVariable Long id,
                                                                              @Valid @RequestBody UpdateDepartmentStatusRequest request) {
        DepartmentItem item = departmentService.updateDepartmentStatus(id, request, getCurrentUserName());
        return ResponseEntity.ok(ApiResponse.ok(item, "Department status updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id, getCurrentUserName());
        return ResponseEntity.ok(ApiResponse.ok(null, "Department deleted successfully"));
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthException(401, "Unauthenticated");
        }
        return authentication.getName();
    }
}

