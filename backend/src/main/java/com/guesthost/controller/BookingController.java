package com.guesthost.controller;

import com.guesthost.dto.BookingDto;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/host/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<Booking>> getBookings(Authentication authentication, @RequestParam String propertyId) {
        return ResponseEntity.ok(bookingService.getBookingsForProperty(authentication.getName(), propertyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(Authentication authentication, @PathVariable String id) {
        return ResponseEntity.ok(bookingService.getBooking(authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(Authentication authentication, @Valid @RequestBody BookingDto dto) {
        return ResponseEntity.ok(bookingService.createBooking(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(Authentication authentication, @PathVariable String id,
                                                 @Valid @RequestBody BookingDto dto) {
        return ResponseEntity.ok(bookingService.updateBooking(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(Authentication authentication, @PathVariable String id) {
        bookingService.deleteBooking(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
