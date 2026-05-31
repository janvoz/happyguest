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
import org.springframework.web.bind.annotation.RestController;

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
        Booking booking = bookingRepository.findById(reference)
                .or(() -> bookingRepository.findAllByPropertyId(propertyId).stream()
                        .filter(b -> reference.equals(b.getId()))
                        .findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        return ResponseEntity.ok(booking);
    }
}

