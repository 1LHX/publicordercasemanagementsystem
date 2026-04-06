package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.CreateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.RoleItem;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateRoleStatusRequest;

import java.util.List;

public interface RoleService {

    List<RoleItem> listRoles(Boolean isActive);

    RoleItem getRoleByCode(String code);

    RoleItem createRole(CreateRoleRequest request, String operatorName);

    RoleItem updateRole(String code, UpdateRoleRequest request, String operatorName);

    RoleItem updateRoleStatus(String code, UpdateRoleStatusRequest request, String operatorName);

    void deleteRole(String code, String operatorName);
}

