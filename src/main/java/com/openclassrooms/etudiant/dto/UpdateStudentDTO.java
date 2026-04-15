package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStudentDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}