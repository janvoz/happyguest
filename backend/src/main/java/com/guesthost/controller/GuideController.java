package com.guesthost.controller;

import com.guesthost.dto.GuideItemDto;
import com.guesthost.model.GuideItem;
import com.guesthost.service.GuideService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping("/api/host/guides")
    public ResponseEntity<List<GuideItem>> getGuides(Authentication authentication, @RequestParam String propertyId) {
        return ResponseEntity.ok(guideService.getGuidesForHost(authentication.getName(), propertyId));
    }

    @PostMapping("/api/host/guides")
    public ResponseEntity<GuideItem> createGuide(Authentication authentication, @Valid @RequestBody GuideItemDto dto) {
        return ResponseEntity.ok(guideService.createGuide(authentication.getName(), dto));
    }

    @PutMapping("/api/host/guides/{id}")
    public ResponseEntity<GuideItem> updateGuide(Authentication authentication, @PathVariable String id,
                                                 @Valid @RequestBody GuideItemDto dto) {
        return ResponseEntity.ok(guideService.updateGuide(authentication.getName(), id, dto));
    }

    @DeleteMapping("/api/host/guides/{id}")
    public ResponseEntity<Void> deleteGuide(Authentication authentication, @PathVariable String id) {
        guideService.deleteGuide(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/public/guest/guides/{propertyId}")
    public ResponseEntity<List<GuideItem>> getPublicGuides(@PathVariable String propertyId) {
        return ResponseEntity.ok(guideService.getPublicGuides(propertyId));
    }
}
