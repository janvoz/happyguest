package com.guesthost.controller;

import com.guesthost.dto.ChatMessageDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.ChatMessage;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.ChatMessageRepository;
import com.guesthost.security.RequiresFeature;
import com.guesthost.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;

    @GetMapping({"/api/public/bookings/{bookingId}/chat", "/api/guest/bookings/{bookingId}/chat"})
    public ResponseEntity<List<ChatMessage>> getPublicChat(@PathVariable String bookingId) {
        ensureBookingExists(bookingId);
        return ResponseEntity.ok(chatMessageRepository.findAllByBookingIdOrderByCreatedAtAsc(bookingId));
    }

    @PostMapping({"/api/public/bookings/{bookingId}/chat", "/api/guest/bookings/{bookingId}/chat"})
    public ResponseEntity<ChatMessage> sendGuestMessage(@PathVariable String bookingId, @Valid @RequestBody ChatMessageDto dto) {
        if (!bookingId.equals(dto.getBookingId())) {
            throw new IllegalArgumentException("Booking id mismatch");
        }
        ensureBookingExists(bookingId);
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .bookingId(bookingId)
                .sender(ChatMessage.Sender.GUEST)
                .messageText(dto.getMessageText().trim())
                .createdAt(Instant.now())
                .build());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/host/bookings/{bookingId}/chat")
    @RequiresFeature("TWO_WAY_CHAT")
    public ResponseEntity<List<ChatMessage>> getHostChat(Authentication authentication, @PathVariable String bookingId) {
        Booking booking = ensureBookingExists(bookingId);
        propertyService.getOwnedProperty(authentication.getName(), booking.getPropertyId());
        return ResponseEntity.ok(chatMessageRepository.findAllByBookingIdOrderByCreatedAtAsc(bookingId));
    }

    @PostMapping("/api/host/bookings/{bookingId}/chat")
    @RequiresFeature("TWO_WAY_CHAT")
    public ResponseEntity<ChatMessage> sendHostMessage(Authentication authentication, @PathVariable String bookingId, @Valid @RequestBody ChatMessageDto dto) {
        if (!bookingId.equals(dto.getBookingId())) {
            throw new IllegalArgumentException("Booking id mismatch");
        }
        Booking booking = ensureBookingExists(bookingId);
        propertyService.getOwnedProperty(authentication.getName(), booking.getPropertyId());
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .bookingId(bookingId)
                .sender(ChatMessage.Sender.HOST)
                .messageText(dto.getMessageText().trim())
                .createdAt(Instant.now())
                .build());
        return ResponseEntity.ok(saved);
    }

    private Booking ensureBookingExists(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
    }
}
