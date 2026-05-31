package com.guesthost.controller;

import com.guesthost.model.Booking;
import com.guesthost.security.RequiresFeature;
import com.guesthost.service.ChannelSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChannelSyncController {

    private final ChannelSyncService channelSyncService;

    @RequiresFeature("UBYPORT_SYNC")
    @PostMapping("/api/host/properties/{propertyId}/channels/sync")
    public ResponseEntity<List<Booking>> syncReservations(Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(channelSyncService.syncForProperty(authentication.getName(), propertyId));
    }

    @PostMapping({"/api/webhooks/airbnb", "/api/webhooks/booking"})
    public ResponseEntity<Map<String, String>> handleChannelWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "accepted", "event", String.valueOf(payload.getOrDefault("event", "unknown"))));
    }
}
