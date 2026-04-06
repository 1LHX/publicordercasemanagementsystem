package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDepartmentRequest {

    @NotBlank(message = "name is required")
    private String name;

    private Long parentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}

