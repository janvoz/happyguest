package com.guesthost.controller;

import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class PublicController {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/api/public/properties/{propertyId}")
    public ResponseEntity<Property> getProperty(@PathVariable String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        return ResponseEntity.ok(property);
    }

    @GetMapping("/api/public/bookings/{bookingId}")
    public ResponseEntity<Booking> getBooking(@PathVariable String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/api/public/properties/{propertyId}/bookings/{reference}")
    public ResponseEntity<Booking> getBookingByReference(@PathVariable String propertyId, @PathVariable String reference) {
        String normalizedReference = reference.trim().toUpperCase(Locale.ROOT);
        Booking booking = bookingRepository.findByPropertyIdAndBookingRefNumber(propertyId, normalizedReference)
                .or(() -> bookingRepository.findAllByPropertyId(propertyId).stream()
                        .filter(b -> normalizedReference.equalsIgnoreCase(b.getId()))
                        .findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/api/public/properties/{propertyId}/bookings/access")
    public ResponseEntity<Booking> getBookingByGate(
            @PathVariable String propertyId,
            @RequestParam String reference,
            @RequestParam String lastName) {
        String normalizedReference = reference.trim().toUpperCase(Locale.ROOT);
        String normalizedLastName = normalizeLastName(lastName);
        Booking booking = bookingRepository.findByPropertyIdAndBookingRefNumber(propertyId, normalizedReference)
                .or(() -> bookingRepository.findAllByPropertyId(propertyId).stream()
                        .filter(b -> normalizedReference.equalsIgnoreCase(b.getId()))
                        .findFirst())
                .filter(b -> normalizeLastName(b.getGuestName()).equals(normalizedLastName))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for provided credentials"));
        return ResponseEntity.ok(booking);
    }

    private String normalizeLastName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String[] tokens = name.trim().split("\\s+");
        return tokens[tokens.length - 1].toLowerCase(Locale.ROOT);
    }
}
