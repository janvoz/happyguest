package com.guesthost.dto;

import com.guesthost.model.GuestMessage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class GuestMessageDto {
    private String id;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    private String bookingId;
    private String guestName;
    private GuestMessage.MessageType messageType;

    @NotBlank(message = "Message content is required")
    private String content;

    @Min(value = 0, message = "Rating cannot be negative")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private int rating;

    private Instant createdAt;
    private boolean read;
}
