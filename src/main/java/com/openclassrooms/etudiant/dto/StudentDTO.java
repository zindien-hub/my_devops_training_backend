package com.openclassrooms.etudiant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}