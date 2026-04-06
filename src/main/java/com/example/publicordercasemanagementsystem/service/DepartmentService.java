package com.example.publicordercasemanagementsystem.service;

import com.example.publicordercasemanagementsystem.dto.CreateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.DepartmentItem;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentRequest;
import com.example.publicordercasemanagementsystem.dto.UpdateDepartmentStatusRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentItem> listDepartments(String name, Boolean isActive, Long parentId);

    DepartmentItem getDepartmentById(Long id);

    DepartmentItem createDepartment(CreateDepartmentRequest request, String operatorName);

    DepartmentItem updateDepartment(Long id, UpdateDepartmentRequest request, String operatorName);

    DepartmentItem updateDepartmentStatus(Long id, UpdateDepartmentStatusRequest request, String operatorName);

    void deleteDepartment(Long id, String operatorName);
}

