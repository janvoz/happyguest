package com.guesthost.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranslationRequest {
    @NotBlank(message = "Text is required")
    private String text;

    @NotBlank(message = "Target language is required")
    @JsonAlias({"lang", "language"})
    private String targetLanguage;
}
