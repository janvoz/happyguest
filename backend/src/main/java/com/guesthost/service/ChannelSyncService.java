package com.guesthost.service;

import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelSyncService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final AirbnbClient airbnbClient;
    private final BookingClient bookingClient;

    @Transactional
    public List<Booking> syncForProperty(String hostEmail, String propertyId) {
        Property property = propertyService.getOwnedProperty(hostEmail, propertyId);
        List<Booking> synced = new ArrayList<>();
        synced.addAll(syncReservations(property, airbnbClient.fetchActiveReservations(propertyId)));
        synced.addAll(syncReservations(property, bookingClient.fetchActiveReservations(propertyId)));
        return synced;
    }

    @Transactional
    public Optional<Booking> syncWebhookReservation(String channel, Map<String, Object> payload) {
        ChannelClient client = resolveClient(channel);
        Optional<ChannelReservation> reservation = client.parseWebhookReservation(payload);
        if (reservation.isEmpty()) {
            return Optional.empty();
        }

        propertyRepository.findById(reservation.get().propertyId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown property in webhook payload"));
        return Optional.of(upsertReservation(reservation.get()));
    }

    public void syncManualBookingChange(Booking booking) {
        if (booking == null || booking.getPropertyId() == null || booking.getPropertyId().isBlank() || booking.getId() == null) {
            return;
        }
        ChannelAvailabilityUpdate update = new ChannelAvailabilityUpdate(
                booking.getPropertyId(),
                booking.getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                Booking.ChannelStatus.MODIFIED
        );
        airbnbClient.pushAvailabilityUpdate(update);
        bookingClient.pushAvailabilityUpdate(update);
    }

    public void syncManualBookingCancellation(Booking booking) {
        if (booking == null || booking.getPropertyId() == null || booking.getPropertyId().isBlank() || booking.getId() == null) {
            return;
        }
        ChannelAvailabilityUpdate update = new ChannelAvailabilityUpdate(
                booking.getPropertyId(),
                booking.getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                Booking.ChannelStatus.CANCELLED
        );
        airbnbClient.pushAvailabilityUpdate(update);
        bookingClient.pushAvailabilityUpdate(update);
    }

    private List<Booking> syncReservations(Property property, List<ChannelReservation> reservations) {
        List<Booking> synced = new ArrayList<>();
        for (ChannelReservation reservation : reservations) {
            if (!property.getId().equals(reservation.propertyId()) || !reservation.isActive()) {
                continue;
            }
            synced.add(upsertReservation(reservation));
        }
        return synced;
    }

    private Booking upsertReservation(ChannelReservation reservation) {
        Booking booking = bookingRepository.findByPropertyIdAndSourceAndExternalReservationId(
                        reservation.propertyId(),
                        reservation.source(),
                        reservation.externalReservationId())
                .orElseGet(() -> bookingRepository.findByPropertyIdAndCheckIn(reservation.propertyId(), reservation.checkIn())
                        .orElseGet(Booking::new));

        if (booking.getId() == null) {
            booking.setDoorCode(generateDoorCode());
            booking.setPreArrivalSent(false);
            booking.setPostDepartureSent(false);
            booking.setRegistrationCompleted(false);
            booking.setBookingRefNumber(generateBookingReference(reservation.source(), reservation.externalReservationId()));
        }

        booking.setPropertyId(reservation.propertyId());
        booking.setGuestName(normalizeGuestName(reservation.guestName()));
        booking.setGuestEmail(reservation.guestEmail());
        booking.setGuestPhone(reservation.guestPhone());
        booking.setCheckIn(reservation.checkIn());
        booking.setCheckOut(reservation.checkOut());
        booking.setTotalPrice(reservation.totalPrice() == null ? BigDecimal.ZERO : reservation.totalPrice());
        booking.setSource(reservation.source());
        booking.setExternalReservationId(reservation.externalReservationId());
        booking.setChannelStatus(reservation.channelStatus());
        booking.setChannelLastSyncedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    private ChannelClient resolveClient(String channel) {
        if ("booking".equalsIgnoreCase(channel) || "booking.com".equalsIgnoreCase(channel)) {
            return bookingClient;
        }
        return airbnbClient;
    }

    private String normalizeGuestName(String guestName) {
        return guestName == null || guestName.isBlank() ? "Imported Guest" : guestName.trim();
    }

    private String generateDoorCode() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private String generateBookingReference(Booking.BookingSource source, String externalReservationId) {
        String normalizedExternal = externalReservationId == null ? "UNKNOWN" : externalReservationId.replaceAll("[^A-Za-z0-9]", "");
        String shortExternal = normalizedExternal.length() > 10 ? normalizedExternal.substring(0, 10) : normalizedExternal;
        return source.name() + "-" + shortExternal;
    }
}
