package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageDto {
    @NotBlank(message = "Booking id is required")
    private String bookingId;

    @NotBlank(message = "Message text is required")
    private String messageText;
}
