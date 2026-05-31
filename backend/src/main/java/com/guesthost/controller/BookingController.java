package com.guesthost.controller;

import com.guesthost.dto.BookingDto;
import com.guesthost.dto.RegistrationInviteResponse;
import com.guesthost.model.Booking;
import com.guesthost.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // nested REST route used by frontend
    @GetMapping("/api/host/properties/{propertyId}/bookings")
    public ResponseEntity<List<Booking>> getBookingsByProperty(Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(bookingService.getBookingsForProperty(authentication.getName(), propertyId));
    }

    // legacy query-param route kept for compatibility
    @GetMapping("/api/host/bookings")
    public ResponseEntity<List<Booking>> getBookings(Authentication authentication, @RequestParam String propertyId) {
        return ResponseEntity.ok(bookingService.getBookingsForProperty(authentication.getName(), propertyId));
    }

    @GetMapping("/api/host/bookings/{id}")
    public ResponseEntity<Booking> getBooking(Authentication authentication, @PathVariable String id) {
        return ResponseEntity.ok(bookingService.getBooking(authentication.getName(), id));
    }

    @PostMapping("/api/host/bookings")
    public ResponseEntity<Booking> createBooking(Authentication authentication, @Valid @RequestBody BookingDto dto) {
        return ResponseEntity.ok(bookingService.createBooking(authentication.getName(), dto));
    }

    @PutMapping("/api/host/bookings/{id}")
    public ResponseEntity<Booking> updateBooking(Authentication authentication, @PathVariable String id,
                                                 @Valid @RequestBody BookingDto dto) {
        return ResponseEntity.ok(bookingService.updateBooking(authentication.getName(), id, dto));
    }

    @PostMapping("/api/host/bookings/{id}/registration-invite")
    public ResponseEntity<RegistrationInviteResponse> sendRegistrationInvite(Authentication authentication, @PathVariable String id) {
        return ResponseEntity.ok(bookingService.sendRegistrationInvite(authentication.getName(), id));
    }

    @DeleteMapping("/api/host/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(Authentication authentication, @PathVariable String id) {
        bookingService.deleteBooking(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
