package com.guesthost.service;

import com.guesthost.dto.MinibarOrderRequest;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.MinibarOrder;
import com.guesthost.model.OrderLine;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.MinibarOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final BookingRepository bookingRepository;
    private final MinibarOrderRepository minibarOrderRepository;
    private final MinibarService minibarService;
    private final StripeClient stripeClient;

    @Value("${app.stripe.application-fee-percent:2}")
    private BigDecimal applicationFeePercent;

    public OrderResult createOrder(MinibarOrderRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));
        if (!booking.getPropertyId().equals(request.getPropertyId())) {
            throw new IllegalArgumentException("Booking does not belong to the provided property");
        }

        List<OrderLine> orderLines = minibarService.buildOrderLinesAndDecrementStock(request.getPropertyId(), request.getItems());
        BigDecimal totalAmount = orderLines.stream()
                .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = totalAmount.multiply(applicationFeePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal hostPayout = totalAmount.subtract(fee).setScale(2, RoundingMode.HALF_UP);

        StripeClient.PaymentIntentResult paymentIntent = stripeClient.createPaymentIntent(
                toCents(totalAmount),
                toCents(fee),
                "usd",
                Map.of(
                        "bookingId", booking.getId(),
                        "propertyId", booking.getPropertyId()
                )
        );

        MinibarOrder order = MinibarOrder.builder()
                .bookingId(booking.getId())
                .propertyId(booking.getPropertyId())
                .items(orderLines)
                .totalAmount(totalAmount)
                .applicationFee(fee)
                .hostPayoutAmount(hostPayout)
                .status(MinibarOrder.OrderStatus.PENDING)
                .stripePaymentIntentId(paymentIntent.id())
                .createdAt(Instant.now())
                .build();
        MinibarOrder saved = minibarOrderRepository.save(order);
        return new OrderResult(saved, paymentIntent.clientSecret());
    }

    public MinibarOrder confirmOrder(String stripePaymentIntentId) {
        MinibarOrder order = minibarOrderRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for payment intent: " + stripePaymentIntentId));
        order.setStatus(MinibarOrder.OrderStatus.PAID);
        return minibarOrderRepository.save(order);
    }

    public List<MinibarOrder> getOrdersForProperty(String hostEmail, String propertyId, PropertyService propertyService) {
        propertyService.getOwnedProperty(hostEmail, propertyId);
        return minibarOrderRepository.findAllByPropertyId(propertyId);
    }

    private long toCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public record OrderResult(MinibarOrder order, String clientSecret) {}
}
