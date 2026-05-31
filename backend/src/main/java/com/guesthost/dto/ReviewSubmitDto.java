package com.guesthost.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewSubmitDto {
    @NotBlank(message = "Booking id is required")
    private String bookingId;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Feedback is required")
    private String feedback;
}
