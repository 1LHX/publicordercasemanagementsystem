package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.CreateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.DepartmentItem;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentStatusRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentItem> listDepartments(String name, Boolean isActive, Long parentId);

    DepartmentItem getDepartmentById(Long id);

    DepartmentItem createDepartment(CreateDepartmentRequest request, Long operatorUserId);

    DepartmentItem updateDepartment(Long id, UpdateDepartmentRequest request, Long operatorUserId);

    DepartmentItem updateDepartmentStatus(Long id, UpdateDepartmentStatusRequest request, Long operatorUserId);

    void deleteDepartment(Long id, Long operatorUserId);
}

