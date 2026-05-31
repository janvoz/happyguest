package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "guest_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestMessage {

    @Id
    private String id;

    private String propertyId;
    private String bookingId;
    private String guestName;
    private MessageType messageType;
    private String content;
    private int rating;
    private Instant createdAt;
    private boolean read;

    public enum MessageType {
        ISSUE,
        FEEDBACK,
        REVIEW
    }
}
