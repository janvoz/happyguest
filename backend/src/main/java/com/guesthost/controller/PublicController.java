package com.guesthost.controller;

import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.PropertyRepository;
import com.guesthost.service.GuestAccessTokenService;
import com.guesthost.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class PublicController {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final GuestAccessTokenService guestAccessTokenService;
    private final TranslationService translationService;

    @GetMapping("/api/public/properties/{propertyId}")
    public ResponseEntity<Property> getProperty(@PathVariable String propertyId,
                                                @RequestParam(required = false) String lang) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        return ResponseEntity.ok(localizeProperty(property, lang));
    }

    @GetMapping("/api/public/bookings/{bookingId}")
    public ResponseEntity<Booking> getBooking(@PathVariable String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/api/public/properties/{propertyId}/bookings/{reference}")
    public ResponseEntity<Booking> getBookingByReference(@PathVariable String propertyId, @PathVariable String reference) {
        String normalizedReference = reference.trim().toUpperCase(Locale.ROOT);
        Booking booking = bookingRepository.findByPropertyIdAndBookingRefNumber(propertyId, normalizedReference)
                .or(() -> bookingRepository.findAllByPropertyId(propertyId).stream()
                        .filter(b -> normalizedReference.equalsIgnoreCase(b.getId()))
                        .findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/api/public/properties/{propertyId}/bookings/access")
    public ResponseEntity<Booking> getBookingByGate(
            @PathVariable String propertyId,
            @RequestParam String reference,
            @RequestParam String lastName) {
        String normalizedReference = reference.trim().toUpperCase(Locale.ROOT);
        String normalizedLastName = normalizeLastName(lastName);
        Booking booking = bookingRepository.findByPropertyIdAndBookingRefNumber(propertyId, normalizedReference)
                .or(() -> bookingRepository.findAllByPropertyId(propertyId).stream()
                        .filter(b -> normalizedReference.equalsIgnoreCase(b.getId()))
                        .findFirst())
                .filter(b -> normalizeLastName(b.getGuestName()).equals(normalizedLastName))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for provided credentials"));
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/api/public/properties/{propertyId}/bookings/access-token")
    public ResponseEntity<Booking> getBookingByAccessToken(@PathVariable String propertyId,
                                                           @RequestParam String token) {
        String bookingId = guestAccessTokenService.resolveBookingId(propertyId, token)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for provided access token"));
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> propertyId.equals(b.getPropertyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for provided access token"));
        return ResponseEntity.ok(booking);
    }

    private Property localizeProperty(Property property, String language) {
        if (language == null || language.isBlank() || "en".equalsIgnoreCase(language) || property.getFaqList() == null) {
            return property;
        }
        return Property.builder()
                .id(property.getId())
                .hostId(property.getHostId())
                .name(property.getName())
                .address(property.getAddress())
                .wifiName(property.getWifiName())
                .wifiPassword(property.getWifiPassword())
                .airbnbReviewUrl(property.getAirbnbReviewUrl())
                .bookingReviewUrl(property.getBookingReviewUrl())
                .icalUrls(property.getIcalUrls())
                .checkoutChecklist(property.getCheckoutChecklist())
                .faqList(property.getFaqList().stream()
                        .map(item -> localizeFaq(item, language))
                        .toList())
                .mapMarkers(property.getMapMarkers())
                .quickContacts(property.getQuickContacts())
                .customDomain(property.getCustomDomain())
                .build();
    }

    private Property.FaqItem localizeFaq(Property.FaqItem item, String language) {
        if (!"cs".equalsIgnoreCase(language)) {
            return Property.FaqItem.builder()
                    .question(translationService.translate(item.getQuestion(), language))
                    .answer(translationService.translate(item.getAnswer(), language))
                    .questionCs(item.getQuestionCs())
                    .answerCs(item.getAnswerCs())
                    .build();
        }
        return Property.FaqItem.builder()
                .question((item.getQuestionCs() != null && !item.getQuestionCs().isBlank()) ? item.getQuestionCs() : item.getQuestion())
                .answer((item.getAnswerCs() != null && !item.getAnswerCs().isBlank()) ? item.getAnswerCs() : item.getAnswer())
                .questionCs(item.getQuestionCs())
                .answerCs(item.getAnswerCs())
                .build();
    }

    private String normalizeLastName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String[] tokens = name.trim().split("\\s+");
        return tokens[tokens.length - 1].toLowerCase(Locale.ROOT);
    }
}
