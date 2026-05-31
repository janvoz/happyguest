package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TranslationBatchRequest {
    @NotBlank(message = "Target language is required")
    private String targetLanguage;

    @NotEmpty(message = "Texts are required")
    private List<String> texts;
}
