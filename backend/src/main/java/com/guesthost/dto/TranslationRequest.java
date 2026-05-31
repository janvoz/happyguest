package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranslationRequest {
    @NotBlank(message = "Text is required")
    private String text;

    @NotBlank(message = "Target language is required")
    private String targetLanguage;
}
