package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BatchDeleteRequest {

    @NotEmpty(message = "ids is required")
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
