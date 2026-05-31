package com.guesthost.service;

import com.guesthost.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChannelReservation(
        String propertyId,
        String externalReservationId,
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        BigDecimal totalPrice,
        Booking.BookingSource source,
        Booking.ChannelStatus channelStatus
) {
    public boolean isActive() {
        return channelStatus == Booking.ChannelStatus.CONFIRMED || channelStatus == Booking.ChannelStatus.PENDING;
    }
}
