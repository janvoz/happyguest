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
import java.util.Optional;

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

    @PostMapping("/api/webhooks/{channel}")
    public ResponseEntity<Map<String, Object>> handleChannelWebhook(@PathVariable String channel, @RequestBody Map<String, Object> payload) {
        Optional<Booking> syncedBooking = channelSyncService.syncWebhookReservation(channel, payload);
        return ResponseEntity.ok(Map.of(
                "status", "accepted",
                "channel", channel,
                "event", String.valueOf(payload.getOrDefault("event", "unknown")),
                "syncedBookingId", syncedBooking.map(Booking::getId).orElse("")
        ));
    }

    @PostMapping("/api/webhooks/airbnb")
    public ResponseEntity<Map<String, Object>> handleAirbnbWebhook(@RequestBody Map<String, Object> payload) {
        return handleChannelWebhook("airbnb", payload);
    }

    @PostMapping("/api/webhooks/booking")
    public ResponseEntity<Map<String, Object>> handleBookingWebhook(@RequestBody Map<String, Object> payload) {
        return handleChannelWebhook("booking", payload);
    }
}
