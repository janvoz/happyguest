package com.guesthost.service;

import com.guesthost.dto.MinibarOrderRequest;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.MinibarOrder;
import com.guesthost.model.OrderLine;
import com.guesthost.model.User;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.MinibarOrderRepository;
import com.guesthost.repository.PropertyRepository;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {
    private static final DateTimeFormatter VARIABLE_SYMBOL_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final BookingRepository bookingRepository;
    private final MinibarOrderRepository minibarOrderRepository;
    private final MinibarService minibarService;
    private final StripeClient stripeClient;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

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

        MinibarOrder.PaymentMethod paymentMethod = resolvePaymentMethod(request.getPaymentMethod());
        StripeClient.PaymentIntentResult paymentIntent = paymentMethod == MinibarOrder.PaymentMethod.STRIPE
                ? stripeClient.createPaymentIntent(
                        toCents(totalAmount),
                        toCents(fee),
                        "usd",
                        Map.of(
                                "bookingId", booking.getId(),
                                "propertyId", booking.getPropertyId()
                        )
                )
                : new StripeClient.PaymentIntentResult("", "");

        MinibarOrder order = MinibarOrder.builder()
                .bookingId(booking.getId())
                .propertyId(booking.getPropertyId())
                .items(orderLines)
                .totalAmount(totalAmount)
                .applicationFee(fee)
                .hostPayoutAmount(hostPayout)
                .status(MinibarOrder.OrderStatus.PENDING)
                .stripePaymentIntentId(paymentMethod == MinibarOrder.PaymentMethod.STRIPE ? paymentIntent.id() : null)
                .variableSymbol(generateVariableSymbol(booking, Instant.now()))
                .paymentMethod(paymentMethod)
                .createdAt(Instant.now())
                .build();
        MinibarOrder saved = minibarOrderRepository.save(order);
        String spaydPayload = paymentMethod == MinibarOrder.PaymentMethod.QR_BANK ? buildSpaydPayload(saved) : "";
        return new OrderResult(saved, paymentIntent.clientSecret(), spaydPayload);
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

    private String generateVariableSymbol(Booking booking, Instant now) {
        String bookingPart = booking.getId() == null ? "000000" : booking.getId().replaceAll("[^0-9]", "");
        if (bookingPart.isBlank()) {
            bookingPart = Integer.toString(Math.abs(booking.getId() == null ? 0 : booking.getId().hashCode()));
        }
        if (bookingPart.length() > 4) {
            bookingPart = bookingPart.substring(bookingPart.length() - 4);
        }
        bookingPart = String.format("%4s", bookingPart).replace(' ', '0');
        return VARIABLE_SYMBOL_DATE.format(now.atZone(ZoneOffset.UTC)) + bookingPart;
    }

    public record OrderResult(MinibarOrder order, String clientSecret, String spaydPayload) {}

    private MinibarOrder.PaymentMethod resolvePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return MinibarOrder.PaymentMethod.STRIPE;
        }
        return MinibarOrder.PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private String buildSpaydPayload(MinibarOrder order) {
        Booking booking = bookingRepository.findById(order.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + order.getBookingId()));
        String hostId = propertyRepository.findById(booking.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + booking.getPropertyId()))
                .getHostId();
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found"));
        if (host.getIban() == null || host.getIban().isBlank()) {
            throw new IllegalStateException("Host IBAN is required for QR bank payments");
        }
        String amount = order.getTotalAmount() == null ? "0.00" : order.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String bic = host.getSwift() == null ? "" : host.getSwift().trim().toUpperCase(Locale.ROOT);
        return "SPD*1.0*ACC:" + host.getIban().replace(" ", "")
                + (bic.isBlank() ? "" : "+BIC:" + bic)
                + "*AM:" + amount
                + "*CC:CZK*X-VS:" + order.getVariableSymbol()
                + "*MSG:Minibar " + order.getBookingId();
    }
}
