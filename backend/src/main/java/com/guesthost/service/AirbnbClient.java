package com.guesthost.service;

import com.guesthost.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AirbnbClient implements ChannelClient {

    private static final String MOCK_ENDPOINT = "https://api.airbnb.mock/v1/graphql";
    private final ConcurrentMap<String, ChannelReservation> syncedCalendar = new ConcurrentHashMap<>();

    @Override
    public List<ChannelReservation> fetchActiveReservations(String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            return List.of();
        }

        List<ChannelReservation> reservations = new ArrayList<>();
        reservations.add(buildDefaultReservation(propertyId));
        syncedCalendar.values().stream()
                .filter(reservation -> propertyId.equals(reservation.propertyId()))
                .filter(ChannelReservation::isActive)
                .forEach(reservations::add);

        return reservations.stream()
                .collect(Collectors.toMap(
                        ChannelReservation::externalReservationId,
                        reservation -> reservation,
                        (left, right) -> right))
                .values()
                .stream()
                .sorted((left, right) -> left.checkIn().compareTo(right.checkIn()))
                .toList();
    }

    @Override
    public Optional<ChannelReservation> parseWebhookReservation(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Optional.empty();
        }
        ChannelReservation reservation = fromPayload(payload, Booking.BookingSource.AIRBNB);
        return reservation.externalReservationId().isBlank() || reservation.propertyId().isBlank()
                ? Optional.empty()
                : Optional.of(reservation);
    }

    @Override
    public void pushAvailabilityUpdate(ChannelAvailabilityUpdate update) {
        if (update == null || update.propertyId() == null || update.propertyId().isBlank()
                || update.bookingId() == null || update.bookingId().isBlank()) {
            return;
        }

        ChannelReservation reservation = new ChannelReservation(
                update.propertyId(),
                "AIRBNB-" + update.bookingId(),
                "Calendar Lock",
                "",
                "",
                update.checkIn(),
                update.checkOut(),
                BigDecimal.ZERO,
                Booking.BookingSource.AIRBNB,
                update.channelStatus()
        );
        syncedCalendar.put(reservation.externalReservationId(), reservation);
        log.info("Mock outbound Airbnb availability sync to {} with headers [{}: ****** and content type {} for booking {}",
                MOCK_ENDPOINT, HttpHeaders.AUTHORIZATION, MediaType.APPLICATION_JSON_VALUE, update.bookingId());
    }

    private ChannelReservation buildDefaultReservation(String propertyId) {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        return new ChannelReservation(
                propertyId,
                "AIRBNB-" + propertyId + "-001",
                "Airbnb Guest",
                "airbnb.guest@example.com",
                "+420700100200",
                tomorrow,
                tomorrow.plusDays(3),
                new BigDecimal("512.40"),
                Booking.BookingSource.AIRBNB,
                Booking.ChannelStatus.CONFIRMED
        );
    }

    static ChannelReservation fromPayload(Map<String, Object> payload, Booking.BookingSource source) {
        return new ChannelReservation(
                text(payload, "propertyId"),
                text(payload, "reservationId", "id"),
                text(payload, "guestName"),
                text(payload, "guestEmail"),
                text(payload, "guestPhone"),
                parseDateTime(payload.get("checkIn")),
                parseDateTime(payload.get("checkOut")),
                parseAmount(payload.get("totalPrice")),
                source,
                parseStatus(payload.get("channelStatus"))
        );
    }

    private static String text(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                String normalized = String.valueOf(value).trim();
                if (!normalized.isBlank()) {
                    return normalized;
                }
            }
        }
        return "";
    }

    private static LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }
        try {
            return LocalDateTime.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        }
    }

    private static BigDecimal parseAmount(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private static Booking.ChannelStatus parseStatus(Object value) {
        if (value == null) {
            return Booking.ChannelStatus.CONFIRMED;
        }
        try {
            return Booking.ChannelStatus.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return Booking.ChannelStatus.CONFIRMED;
        }
    }
}
