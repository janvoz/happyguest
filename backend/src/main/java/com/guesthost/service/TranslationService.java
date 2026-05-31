package com.guesthost.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^[a-zA-Z-]{2,10}$");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.google.translate-api-key:}")
    private String apiKey;

    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank()
                || targetLang == null
                || targetLang.isBlank()
                || !LANGUAGE_PATTERN.matcher(targetLang).matches()
                || apiKey == null
                || apiKey.isBlank()) {
            return text;
        }
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://translation.googleapis.com/language/translate/v2")
                    .queryParam("key", apiKey)
                    .build(true)
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> payload = Map.of(
                    "q", text,
                    "target", targetLang
            );
            String response = restTemplate.postForObject(url, new HttpEntity<>(payload, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode translated = root.path("data").path("translations").path(0).path("translatedText");
            return translated.isMissingNode() ? text : translated.asText(text);
        } catch (Exception ex) {
            log.warn("Translation failed, returning original text: {}", ex.getMessage());
            return text;
        }
    }
}
