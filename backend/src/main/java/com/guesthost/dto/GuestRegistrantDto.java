package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestRegistrantDto {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Citizenship is required")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Citizenship must be a valid ISO 3166-1 alpha-2 country code (e.g. US, DE, FR)")
    private String citizenship;

    @NotBlank(message = "Document number is required")
    @Size(min = 2, max = 50, message = "Document number must be between 2 and 50 characters")
    private String documentNumber;

    @Pattern(regexp = "^(PASSPORT|ID_CARD|RESIDENCE_PERMIT|VISA|OTHER)$",
             message = "Document type must be one of: PASSPORT, ID_CARD, RESIDENCE_PERMIT, VISA, OTHER")
    private String documentType;

    @Pattern(regexp = "^([A-Za-z]{2})?$", message = "Document issuing country must be a valid ISO 3166-1 alpha-2 country code")
    private String documentIssuingCountry;

    private LocalDate documentExpiry;

    private String gender;

    @NotBlank(message = "Address is required")
    private String address;
}
