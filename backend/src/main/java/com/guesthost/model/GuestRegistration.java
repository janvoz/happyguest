package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "guest_registrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestRegistration {

    @Id
    private String id;

    private String bookingId;
    private String propertyId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String citizenship;
    private String documentNumber;
    private String address;
    private Instant createdAt;
}
