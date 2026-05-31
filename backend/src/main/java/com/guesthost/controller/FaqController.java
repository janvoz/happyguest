package com.guesthost.controller;

import com.guesthost.model.Property;
import com.guesthost.repository.PropertyRepository;
import com.guesthost.service.PropertyService;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/host/properties/{propertyId}/faq")
@RequiredArgsConstructor
public class FaqController {

    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    @GetMapping
    public ResponseEntity<List<Property.FaqItem>> getFaqList(
            Authentication authentication,
            @PathVariable String propertyId) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        return ResponseEntity.ok(property.getFaqList());
    }

    @PutMapping
    public ResponseEntity<List<Property.FaqItem>> replaceFaqList(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestBody List<Property.FaqItem> faqList) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        property.setFaqList(faqList != null ? faqList : new ArrayList<>());
        propertyRepository.save(property);
        return ResponseEntity.ok(property.getFaqList());
    }

    @PostMapping
    public ResponseEntity<List<Property.FaqItem>> addFaqItem(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestBody Property.FaqItem item) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        List<Property.FaqItem> updated = new ArrayList<>(property.getFaqList());
        updated.add(item);
        property.setFaqList(updated);
        propertyRepository.save(property);
        return ResponseEntity.ok(property.getFaqList());
    }

    @PutMapping("/{index}")
    public ResponseEntity<List<Property.FaqItem>> updateFaqItem(
            Authentication authentication,
            @PathVariable String propertyId,
            @PathVariable int index,
            @RequestBody Property.FaqItem item) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        List<Property.FaqItem> updated = new ArrayList<>(property.getFaqList());
        if (index < 0 || index >= updated.size()) {
            return ResponseEntity.badRequest().build();
        }
        updated.set(index, item);
        property.setFaqList(updated);
        propertyRepository.save(property);
        return ResponseEntity.ok(property.getFaqList());
    }

    @DeleteMapping("/{index}")
    public ResponseEntity<List<Property.FaqItem>> deleteFaqItem(
            Authentication authentication,
            @PathVariable String propertyId,
            @PathVariable int index) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        List<Property.FaqItem> updated = new ArrayList<>(property.getFaqList());
        if (index < 0 || index >= updated.size()) {
            return ResponseEntity.badRequest().build();
        }
        updated.remove(index);
        property.setFaqList(updated);
        propertyRepository.save(property);
        return ResponseEntity.ok(property.getFaqList());
    }
}
