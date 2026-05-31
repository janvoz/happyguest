package com.guesthost.controller;

import com.guesthost.dto.ReviewSubmitDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.GuestMessage;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.GuestMessageRepository;
import com.guesthost.repository.PropertyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final GuestMessageRepository guestMessageRepository;

    @PostMapping("/api/public/guest/review")
    public ResponseEntity<Map<String, Object>> submitReview(@Valid @RequestBody ReviewSubmitDto dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + dto.getBookingId()));
        if (!booking.getPropertyId().equals(dto.getPropertyId())) {
            throw new IllegalArgumentException("Booking does not belong to the provided property");
        }
        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + dto.getPropertyId()));

        GuestMessage.MessageType type = dto.getRating() >= 4 ? GuestMessage.MessageType.REVIEW : GuestMessage.MessageType.FEEDBACK;
        GuestMessage message = GuestMessage.builder()
                .propertyId(dto.getPropertyId())
                .bookingId(dto.getBookingId())
                .guestName(booking.getGuestName())
                .messageType(type)
                .content(dto.getFeedback())
                .rating(dto.getRating())
                .createdAt(Instant.now())
                .read(false)
                .build();
        guestMessageRepository.save(message);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("saved", true);
        response.put("messageType", type);
        if (dto.getRating() >= 4) {
            String redirectUrl = property.getAirbnbReviewUrl() != null && !property.getAirbnbReviewUrl().isBlank()
                    ? property.getAirbnbReviewUrl()
                    : property.getBookingReviewUrl();
            response.put("redirectUrl", redirectUrl);
        }
        return ResponseEntity.ok(response);
    }
}
