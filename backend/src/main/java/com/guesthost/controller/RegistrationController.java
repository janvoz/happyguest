package com.guesthost.controller;

import com.guesthost.dto.GuestRegistrationDto;
import com.guesthost.model.GuestRegistration;
import com.guesthost.security.RequiresFeature;
import com.guesthost.service.PropertyService;
import com.guesthost.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final PropertyService propertyService;

    @PostMapping({"/api/guest/registrations", "/api/public/guest/register"})
    public ResponseEntity<List<GuestRegistration>> registerGuests(@Valid @RequestBody GuestRegistrationDto dto) {
        return ResponseEntity.ok(registrationService.saveRegistrations(dto));
    }

    @GetMapping("/api/host/properties/{propertyId}/logbook")
    @RequiresFeature("LEGAL_LOGBOOK")
    public ResponseEntity<List<GuestRegistration>> getLogbook(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        return ResponseEntity.ok(registrationService.getLogbook(propertyId, from, to));
    }

    @GetMapping("/api/host/properties/{propertyId}/logbook/export")
    @RequiresFeature("LEGAL_LOGBOOK")
    public ResponseEntity<byte[]> exportByProperty(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        byte[] payload = registrationService.exportCsv(propertyId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("guest-logbook.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(payload);
    }

    @GetMapping("/api/host/properties/{propertyId}/logbook/export/ubyport")
    @RequiresFeature("UBYPORT_SYNC")
    public ResponseEntity<byte[]> exportUbyportByProperty(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        byte[] payload = registrationService.exportUbyportXml(propertyId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("ubyport-export.xml").build().toString())
                .contentType(MediaType.APPLICATION_XML)
                .body(payload);
    }

    @GetMapping("/api/host/logbook/export")
    @RequiresFeature("LEGAL_LOGBOOK")
    public ResponseEntity<byte[]> export(
            Authentication authentication,
            @RequestParam String propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        propertyService.getOwnedProperty(authentication.getName(), propertyId);
        byte[] payload = registrationService.exportCsv(propertyId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("guest-logbook.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(payload);
    }
}
