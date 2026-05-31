package com.guesthost.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.guesthost.dto.PropertyDto;
import com.guesthost.model.Property;
import com.guesthost.service.PropertyService;
import com.guesthost.service.WelcomeSheetPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/host/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final WelcomeSheetPdfService welcomeSheetPdfService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @GetMapping
    public ResponseEntity<List<Property>> getProperties(Authentication authentication) {
        return ResponseEntity.ok(propertyService.getPropertiesForHost(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(Authentication authentication, @PathVariable String id) {
        return ResponseEntity.ok(propertyService.getPropertyForHost(authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(Authentication authentication, @Valid @RequestBody PropertyDto dto) {
        return ResponseEntity.ok(propertyService.createProperty(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(Authentication authentication, @PathVariable String id,
                                                   @Valid @RequestBody PropertyDto dto) {
        return ResponseEntity.ok(propertyService.updateProperty(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(Authentication authentication, @PathVariable String id) {
        propertyService.deleteProperty(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generate a QR code data URL for the property's guest portal URL.
     * Used by the Welcome Sign print feature in the host dashboard.
     */
    @GetMapping("/{id}/portal-qr")
    public ResponseEntity<Map<String, String>> getPortalQr(Authentication authentication, @PathVariable String id) {
        propertyService.getOwnedProperty(authentication.getName(), id);
        String portalUrl = frontendUrl + "/guest/portal/" + id;
        String dataUrl = buildQrDataUrl(portalUrl);
        return ResponseEntity.ok(Map.of("qrDataUrl", dataUrl, "portalUrl", portalUrl));
    }

    @GetMapping(value = "/{id}/welcome-sheet.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadWelcomeSheetPdf(Authentication authentication, @PathVariable String id) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), id);
        String portalUrl = frontendUrl + "/guest/portal/" + id;
        byte[] pdf = welcomeSheetPdfService.generate(property, portalUrl);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"welcome-sheet-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String buildQrDataUrl(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 320, 320);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | java.io.IOException ex) {
            throw new IllegalStateException("Failed to generate QR code", ex);
        }
    }
}
