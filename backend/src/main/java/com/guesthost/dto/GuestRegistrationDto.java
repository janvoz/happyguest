package com.guesthost.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GuestRegistrationDto {
    @NotBlank(message = "Booking id is required")
    private String bookingId;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @Valid
    @NotEmpty(message = "At least one guest is required")
    private List<GuestRegistrantDto> guests = new ArrayList<>();
}
