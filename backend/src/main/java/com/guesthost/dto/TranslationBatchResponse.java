package com.guesthost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TranslationBatchResponse {
    private List<String> translatedTexts;
}
