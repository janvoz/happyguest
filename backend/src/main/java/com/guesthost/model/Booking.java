package com.guesthost.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    private String propertyId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal totalPrice;
    private String doorCode;
    private boolean preArrivalSent;
    private boolean postDepartureSent;

    @Field("isRegistrationCompleted")
    @JsonProperty("isRegistrationCompleted")
    private boolean registrationCompleted;

    private String bookingRefNumber;
    private BookingSource source;
    private String externalReservationId;
    private ChannelStatus channelStatus;
    private LocalDateTime channelLastSyncedAt;

    public enum BookingSource {
        AIRBNB,
        BOOKING,
        ICAL,
        MANUAL
    }

    public enum ChannelStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        MODIFIED
    }
}
