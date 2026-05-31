package com.guesthost.controller;

import com.guesthost.dto.TranslationRequest;
import com.guesthost.dto.TranslationResponse;
import com.guesthost.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/api/public/translate")
    public ResponseEntity<TranslationResponse> translate(@Valid @RequestBody TranslationRequest request) {
        return ResponseEntity.ok(new TranslationResponse(
                translationService.translate(request.getText(), request.getTargetLanguage())
        ));
    }
}
