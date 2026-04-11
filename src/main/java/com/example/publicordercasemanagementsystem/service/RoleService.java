package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.CreateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.RoleItem;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleStatusRequest;

import java.util.List;

public interface RoleService {

    List<RoleItem> listRoles(Boolean isActive);

    RoleItem getRoleByCode(String code);

    RoleItem createRole(CreateRoleRequest request, Long operatorUserId);

    RoleItem updateRole(String code, UpdateRoleRequest request, Long operatorUserId);

    RoleItem updateRoleStatus(String code, UpdateRoleStatusRequest request, Long operatorUserId);

    void deleteRole(String code, Long operatorUserId);
}

