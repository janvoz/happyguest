package com.guesthost.controller;

import com.guesthost.dto.TranslationBatchRequest;
import com.guesthost.dto.TranslationBatchResponse;
import com.guesthost.dto.TranslationRequest;
import com.guesthost.dto.TranslationResponse;
import com.guesthost.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping({"/api/guest/translate", "/api/public/translate"})
    public ResponseEntity<TranslationResponse> translate(@Valid @RequestBody TranslationRequest request) {
        return ResponseEntity.ok(new TranslationResponse(
                translationService.translate(request.getText(), request.getTargetLanguage())
        ));
    }

    @PostMapping("/api/public/translate/batch")
    public ResponseEntity<TranslationBatchResponse> translateBatch(@Valid @RequestBody TranslationBatchRequest request) {
        List<String> translated = request.getTexts().stream()
                .map(text -> translationService.translate(text, request.getTargetLanguage()))
                .toList();
        return ResponseEntity.ok(new TranslationBatchResponse(translated));
    }
}
