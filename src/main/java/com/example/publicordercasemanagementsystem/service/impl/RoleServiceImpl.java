package com.example.publicordercasemanagementsystem.service.impl;

import com.example.publicordercasemanagementsystem.dto.CreateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.RoleItem;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleStatusRequest;
import com.example.publicordercasemanagementsystem.exception.AuthException;
import com.example.publicordercasemanagementsystem.mapper.RoleMapper;
import com.example.publicordercasemanagementsystem.mapper.UserMapper;
import com.example.publicordercasemanagementsystem.pojo.Role;
import com.example.publicordercasemanagementsystem.pojo.User;
import com.example.publicordercasemanagementsystem.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RoleServiceImpl implements RoleService {

    private static final String ADMIN_ROLE = "admin";
    private static final String PERMISSION_DENIED_MESSAGE = "当前角色无此操作权限。";

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    public RoleServiceImpl(RoleMapper roleMapper, UserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<RoleItem> listRoles(Boolean isActive) {
        List<Role> roles = roleMapper.findAll(isActive);
        List<RoleItem> items = new ArrayList<>(roles.size());
        for (Role role : roles) {
            items.add(toRoleItem(role));
        }
        return items;
    }

    @Override
    public RoleItem getRoleByCode(String code) {
        return toRoleItem(requireRole(code));
    }

    @Override
    public RoleItem createRole(CreateRoleRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);

        String code = normalizeRoleCode(request.getCode());
        if (roleMapper.findByCode(code) != null) {
            throw new AuthException(400, "Role code already exists");
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(request.getName().trim());
        role.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        role.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
        roleMapper.insert(role);
        return toRoleItem(requireRole(code));
    }

    @Override
    public RoleItem updateRole(String code, UpdateRoleRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);

        Role existing = requireRole(code);
        existing.setName(request.getName().trim());
        existing.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        roleMapper.updateByCode(existing);
        return toRoleItem(requireRole(existing.getCode()));
    }

    @Override
    public RoleItem updateRoleStatus(String code, UpdateRoleStatusRequest request, Long operatorUserId) {
        requireAdmin(operatorUserId);

        Role existing = requireRole(code);
        if (ADMIN_ROLE.equals(existing.getCode()) && !Boolean.TRUE.equals(request.getIsActive())) {
            throw new AuthException(400, "Admin role cannot be disabled");
        }
        if (!Boolean.TRUE.equals(request.getIsActive()) && roleMapper.countUsersByRoleCode(existing.getCode()) > 0) {
            throw new AuthException(400, "Role is in use by users and cannot be disabled");
        }
        roleMapper.updateActiveByCode(existing.getCode(), request.getIsActive());
        return toRoleItem(requireRole(existing.getCode()));
    }

    @Override
    public void deleteRole(String code, Long operatorUserId) {
        requireAdmin(operatorUserId);

        Role existing = requireRole(code);
        if (ADMIN_ROLE.equals(existing.getCode())) {
            throw new AuthException(400, "Admin role cannot be deleted");
        }
        if (roleMapper.countUsersByRoleCode(existing.getCode()) > 0) {
            throw new AuthException(400, "Role is in use by users and cannot be deleted");
        }
        roleMapper.deleteByCode(existing.getCode());
    }

    private Role requireRole(String code) {
        if (code == null || code.isBlank()) {
            throw new AuthException(400, "Role code is required");
        }
        String normalizedCode = normalizeRoleCode(code);
        Role role = roleMapper.findByCode(normalizedCode);
        if (role == null) {
            throw new AuthException(404, "Role not found");
        }
        return role;
    }

    private void requireAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            throw new AuthException(401, "Unauthenticated");
        }
        User operator = userMapper.findById(operatorUserId);
        if (operator == null || !ADMIN_ROLE.equals(operator.getRole())) {
            throw new AuthException(403, PERMISSION_DENIED_MESSAGE);
        }
    }

    private String normalizeRoleCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }

    private RoleItem toRoleItem(Role role) {
        RoleItem item = new RoleItem();
        item.setCode(role.getCode());
        item.setName(role.getName());
        item.setSortOrder(role.getSortOrder());
        item.setIsActive(role.getIsActive());
        item.setCreatedAt(role.getCreatedAt());
        item.setUpdatedAt(role.getUpdatedAt());
        return item;
    }
}

