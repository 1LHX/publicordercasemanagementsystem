package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.CreateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.DepartmentItem;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentStatusRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.DepartmentMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.Department;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final String ADMIN_ROLE = "admin";
    private static final String PERMISSION_DENIED_MESSAGE = "当前角色无此操作权限。";

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper, UserMapper userMapper) {
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<DepartmentItem> listDepartments(String name, Boolean isActive, Long parentId) {
        List<Department> departments = departmentMapper.findAll(name, isActive, parentId);
        List<DepartmentItem> items = new ArrayList<>(departments.size());
        for (Department department : departments) {
            items.add(toItem(department));
        }
        return items;
    }

    @Override
    public DepartmentItem getDepartmentById(Long id) {
        return toItem(requireDepartment(id));
    }

    @Override
    public DepartmentItem createDepartment(CreateDepartmentRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);
        String name = normalizeName(request.getName());
        if (departmentMapper.findByName(name) != null) {
            throw new AuthException(400, "Department name already exists");
        }
        validateParent(request.getParentId(), null);

        Department department = new Department();
        department.setName(name);
        department.setParentId(request.getParentId());
        department.setIsActive(true);
        departmentMapper.insert(department);
        return toItem(requireDepartment(department.getId()));
    }

    @Override
    public DepartmentItem updateDepartment(Long id, UpdateDepartmentRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);
        Department existing = requireDepartment(id);

        String name = normalizeName(request.getName());
        if (departmentMapper.countByNameExcludeId(name, id) > 0) {
            throw new AuthException(400, "Department name already exists");
        }
        validateParent(request.getParentId(), id);
        existing.setName(name);
        existing.setParentId(request.getParentId());
        departmentMapper.updateById(existing);
        return toItem(requireDepartment(id));
    }

    @Override
    public DepartmentItem updateDepartmentStatus(Long id, UpdateDepartmentStatusRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);
        requireDepartment(id);

        if (!Boolean.TRUE.equals(request.getIsActive())) {
            if (departmentMapper.countChildrenByParentId(id) > 0) {
                throw new AuthException(400, "Department has child departments and cannot be disabled");
            }
            if (departmentMapper.countUsersByDepartmentId(id) > 0 || departmentMapper.countCasesByDepartmentId(id) > 0) {
                throw new AuthException(400, "Department is in use and cannot be disabled");
            }
        }
        departmentMapper.updateActiveById(id, request.getIsActive());
        return toItem(requireDepartment(id));
    }

    @Override
    public void deleteDepartment(Long id, Long operatorUserId) {
        requireAdmin(operatorUserId);
        requireDepartment(id);

        if (departmentMapper.countChildrenByParentId(id) > 0) {
            throw new AuthException(400, "Department has child departments and cannot be deleted");
        }
        if (departmentMapper.countUsersByDepartmentId(id) > 0 || departmentMapper.countCasesByDepartmentId(id) > 0) {
            throw new AuthException(400, "Department is in use and cannot be deleted");
        }
        departmentMapper.deleteById(id);
    }

    private Department requireDepartment(Long id) {
        if (id == null) {
            throw new AuthException(400, "Department id is required");
        }
        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new AuthException(404, "Department not found");
        }
        return department;
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (currentId != null && currentId.equals(parentId)) {
            throw new AuthException(400, "Department cannot be its own parent");
        }
        Department parent = requireDepartment(parentId);
        if (!Boolean.TRUE.equals(parent.getIsActive())) {
            throw new AuthException(400, "Parent department is inactive");
        }
        Long ancestorId = parent.getParentId();
        while (ancestorId != null) {
            if (currentId != null && currentId.equals(ancestorId)) {
                throw new AuthException(400, "Department hierarchy contains a cycle");
            }
            Department ancestor = departmentMapper.findById(ancestorId);
            if (ancestor == null) {
                break;
            }
            ancestorId = ancestor.getParentId();
        }
    }

    private User requireAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            throw new AuthException(401, "Unauthenticated");
        }
        User operator = userMapper.findById(operatorUserId);
        if (operator == null || !ADMIN_ROLE.equals(operator.getRole())) {
            throw new AuthException(403, PERMISSION_DENIED_MESSAGE);
        }
        return operator;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private DepartmentItem toItem(Department department) {
        DepartmentItem item = new DepartmentItem();
        item.setId(department.getId());
        item.setName(department.getName());
        item.setParentId(department.getParentId());
        item.setParentName(department.getParentName());
        item.setIsActive(department.getIsActive());
        item.setCreatedAt(department.getCreatedAt());
        item.setUpdatedAt(department.getUpdatedAt());
        return item;
    }
}

