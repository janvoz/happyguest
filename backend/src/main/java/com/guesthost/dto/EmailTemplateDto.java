package com.guesthost.dto;

import com.guesthost.model.EmailTemplate.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailTemplateDto {

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @NotNull(message = "Trigger type is required")
    private TriggerType triggerType;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "HTML body is required")
    private String htmlBody;
}
