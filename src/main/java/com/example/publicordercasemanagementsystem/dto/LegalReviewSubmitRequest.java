package com.example.publicordercasemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

public class LegalReviewSubmitRequest {

    @NotBlank(message = "comment is required")
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

