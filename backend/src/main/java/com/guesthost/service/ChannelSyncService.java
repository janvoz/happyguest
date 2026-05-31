package com.guesthost.service;

import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelSyncService {

    private final PropertyService propertyService;
    private final BookingService bookingService;
    private final AirbnbClient airbnbClient;
    private final BookingClient bookingClient;

    public List<Booking> syncForProperty(String hostEmail, String propertyId) {
        Property property = propertyService.getOwnedProperty(hostEmail, propertyId);
        List<Booking> created = new ArrayList<>();
        for (AirbnbClient.RemoteReservation reservation : airbnbClient.fetchReservations(propertyId)) {
            tryCreateImported(created, property, reservation);
        }
        for (AirbnbClient.RemoteReservation reservation : bookingClient.fetchReservations(propertyId)) {
            tryCreateImported(created, property, reservation);
        }
        return created;
    }

    private void tryCreateImported(List<Booking> created, Property property, AirbnbClient.RemoteReservation reservation) {
        try {
            created.add(bookingService.createImportedBooking(
                    property,
                    reservation.guestName(),
                    reservation.guestEmail(),
                    reservation.checkIn(),
                    reservation.checkOut(),
                    reservation.source()
            ));
        } catch (IllegalStateException ignored) {
            // already imported
        }
    }
}
