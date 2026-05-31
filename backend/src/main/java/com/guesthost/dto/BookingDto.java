package com.guesthost.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private String id;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @NotBlank(message = "Guest name is required")
    private String guestName;

    @Email(message = "Guest email must be valid")
    private String guestEmail;

    @NotNull(message = "Check-in is required")
    private LocalDateTime checkIn;

    @NotNull(message = "Check-out is required")
    private LocalDateTime checkOut;

    private String source;
}
