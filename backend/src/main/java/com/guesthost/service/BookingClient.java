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
public class BookingClient implements ChannelClient {

    private static final String MOCK_ENDPOINT = "https://distribution.booking.mock/v2/reservations";
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
        ChannelReservation reservation = AirbnbClient.fromPayload(payload, Booking.BookingSource.BOOKING);
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
                "BOOKING-" + update.bookingId(),
                "Calendar Lock",
                "",
                "",
                update.checkIn(),
                update.checkOut(),
                BigDecimal.ZERO,
                Booking.BookingSource.BOOKING,
                update.channelStatus()
        );
        syncedCalendar.put(reservation.externalReservationId(), reservation);
        log.info("Mock outbound Booking.com availability sync to {} with headers [{}: ****** and content type {} for booking {}",
                MOCK_ENDPOINT, HttpHeaders.AUTHORIZATION, MediaType.APPLICATION_JSON_VALUE, update.bookingId());
    }

    private ChannelReservation buildDefaultReservation(String propertyId) {
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MINUTES);
        return new ChannelReservation(
                propertyId,
                "BOOKING-" + propertyId + "-001",
                "Booking Guest",
                "booking.guest@example.com",
                "+420700100300",
                dayAfterTomorrow,
                dayAfterTomorrow.plusDays(2),
                new BigDecimal("431.25"),
                Booking.BookingSource.BOOKING,
                Booking.ChannelStatus.CONFIRMED
        );
    }
}
