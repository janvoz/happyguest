package com.guesthost.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MinibarItemDto {
    private String id;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock count cannot be negative")
    private int stockCount;

    private String imageUrl;
}
