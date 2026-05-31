package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestRegistrantDto {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Citizenship is required")
    private String citizenship;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Address is required")
    private String address;
}
