package com.guesthost.controller;

import com.guesthost.model.Property;
import com.guesthost.repository.PropertyRepository;
import com.guesthost.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
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
        property.setFaqList(normalizeList(faqList));
        propertyRepository.save(property);
        return ResponseEntity.ok(property.getFaqList());
    }

    @PostMapping
    public ResponseEntity<List<Property.FaqItem>> addFaqItem(
            Authentication authentication,
            @PathVariable String propertyId,
            @RequestBody Property.FaqItem item) {
        Property property = propertyService.getOwnedProperty(authentication.getName(), propertyId);
        if (!isValid(item)) {
            return ResponseEntity.badRequest().build();
        }
        List<Property.FaqItem> updated = new ArrayList<>(property.getFaqList());
        updated.add(normalize(item));
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
        if (!isValid(item)) {
            return ResponseEntity.badRequest().build();
        }
        updated.set(index, normalize(item));
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

    private List<Property.FaqItem> normalizeList(List<Property.FaqItem> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return source.stream()
                .filter(this::isValid)
                .map(this::normalize)
                .toList();
    }

    private boolean isValid(Property.FaqItem item) {
        return item != null
                && StringUtils.hasText(item.getQuestion())
                && StringUtils.hasText(item.getAnswer());
    }

    private Property.FaqItem normalize(Property.FaqItem item) {
        return Property.FaqItem.builder()
                .question(item.getQuestion().trim())
                .answer(item.getAnswer().trim())
                .questionCs(item.getQuestionCs() != null ? item.getQuestionCs().trim() : null)
                .answerCs(item.getAnswerCs() != null ? item.getAnswerCs().trim() : null)
                .build();
    }
}
