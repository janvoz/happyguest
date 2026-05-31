package com.guesthost.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderLineDto {
    @NotBlank(message = "Item id is required")
    private String itemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
