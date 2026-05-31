package com.guesthost.controller;

import com.guesthost.dto.EmailTemplateDto;
import com.guesthost.model.EmailTemplate;
import com.guesthost.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping("/api/host/properties/{propertyId}/templates")
    public ResponseEntity<List<EmailTemplate>> getTemplatesByProperty(Authentication authentication,
                                                                       @PathVariable String propertyId) {
        return ResponseEntity.ok(emailTemplateService.getTemplatesForHost(authentication.getName(), propertyId));
    }

    @PostMapping("/api/host/templates")
    public ResponseEntity<EmailTemplate> createTemplate(Authentication authentication,
                                                         @Valid @RequestBody EmailTemplateDto dto) {
        return ResponseEntity.ok(emailTemplateService.createTemplate(authentication.getName(), dto));
    }

    @PutMapping("/api/host/templates/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(Authentication authentication,
                                                         @PathVariable String id,
                                                         @Valid @RequestBody EmailTemplateDto dto) {
        return ResponseEntity.ok(emailTemplateService.updateTemplate(authentication.getName(), id, dto));
    }

    @DeleteMapping("/api/host/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(Authentication authentication, @PathVariable String id) {
        emailTemplateService.deleteTemplate(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
