package com.guesthost.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationInviteResponse {
    private String bookingId;
    private String recipient;
    private boolean sent;
    private String status;
    private String message;
}

