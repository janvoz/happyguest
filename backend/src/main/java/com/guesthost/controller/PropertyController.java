package com.guesthost.controller;

import com.guesthost.dto.PropertyDto;
import com.guesthost.model.Property;
import com.guesthost.service.PropertyService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/host/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

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
}
