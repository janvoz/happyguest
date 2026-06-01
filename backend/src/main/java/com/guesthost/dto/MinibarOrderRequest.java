package com.guesthost.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MinibarOrderRequest {
    @NotBlank(message = "Booking id is required")
    private String bookingId;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @Valid
    @NotEmpty(message = "At least one order item is required")
    private List<OrderLineDto> items = new ArrayList<>();

    private String paymentMethod;
}
