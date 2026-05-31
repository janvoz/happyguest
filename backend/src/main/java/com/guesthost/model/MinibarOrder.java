package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "minibar_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinibarOrder {

    @Id
    private String id;

    private String bookingId;
    private String propertyId;

    @Builder.Default
    private List<OrderLine> items = new ArrayList<>();

    private BigDecimal totalAmount;
    private BigDecimal applicationFee;
    private BigDecimal hostPayoutAmount;
    private OrderStatus status;
    private String stripePaymentIntentId;
    private Instant createdAt;

    public enum OrderStatus {
        PENDING,
        PAID,
        REFUNDED
    }
}
