package com.guesthost.controller;

import com.guesthost.dto.GuestMessageDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.GuestMessage;
import com.guesthost.repository.GuestMessageRepository;
import com.guesthost.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class MessageController {

    private final GuestMessageRepository guestMessageRepository;
    private final PropertyService propertyService;

    @PostMapping({"/api/guest/messages", "/api/public/guest/messages"})
    public ResponseEntity<GuestMessage> createMessage(@Valid @RequestBody GuestMessageDto dto) {
        GuestMessage message = GuestMessage.builder()
                .propertyId(dto.getPropertyId())
                .bookingId(dto.getBookingId())
                .guestName(dto.getGuestName())
                .messageType(dto.getMessageType() == null ? GuestMessage.MessageType.ISSUE : dto.getMessageType())
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(Instant.now())
                .read(false)
                .build();
        return ResponseEntity.ok(guestMessageRepository.save(message));
    }

    @GetMapping("/api/host/properties/{propertyId}/messages")
    public ResponseEntity<List<GuestMessage>> getMessagesByProperty(Authentication authentication, @PathVariable String propertyId) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        return ResponseEntity.ok(guestMessageRepository.findAllByPropertyIdOrderByCreatedAtDesc(propertyId));
    }

    @GetMapping("/api/host/messages/{propertyId}")
    public ResponseEntity<List<GuestMessage>> getMessages(Authentication authentication, @PathVariable String propertyId) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        return ResponseEntity.ok(guestMessageRepository.findAllByPropertyIdOrderByCreatedAtDesc(propertyId));
    }

    @PatchMapping("/api/host/messages/{id}/read")
    public ResponseEntity<GuestMessage> markRead(Authentication authentication, @PathVariable String id) {
        GuestMessage message = guestMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest message not found: " + id));
        propertyService.getOwnedProperty(authentication.getName(), message.getPropertyId());
        message.setRead(true);
        return ResponseEntity.ok(guestMessageRepository.save(message));
    }
}
