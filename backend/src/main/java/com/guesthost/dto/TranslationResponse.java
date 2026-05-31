package com.guesthost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranslationResponse {
    private String translatedText;
}
