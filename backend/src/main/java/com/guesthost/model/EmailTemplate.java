package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "email_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    private String id;

    private String hostId;
    private String propertyId;
    private TriggerType triggerType;
    private String subject;
    private String htmlBody;

    public enum TriggerType {
        PRE_ARRIVAL,
        POST_DEPARTURE
    }
}
