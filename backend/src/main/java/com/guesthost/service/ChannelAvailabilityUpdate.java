package com.guesthost.service;

import com.guesthost.model.Booking;

import java.time.LocalDateTime;

public record ChannelAvailabilityUpdate(
        String propertyId,
        String bookingId,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Booking.ChannelStatus channelStatus
) {
}
