package com.guesthost.service;

import com.guesthost.dto.PropertyDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Property;
import com.guesthost.model.User;
import com.guesthost.repository.PropertyRepository;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public List<Property> getPropertiesForHost(String hostEmail) {
        User host = getUserByEmail(hostEmail);
        return propertyRepository.findAllByHostId(host.getId());
    }

    public Property getPropertyForHost(String hostEmail, String propertyId) {
        Property property = getOwnedProperty(hostEmail, propertyId);
        return propertyRepository.findById(property.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
    }

    public Property createProperty(String hostEmail, PropertyDto dto) {
        User host = getUserByEmail(hostEmail);
        long existingCount = propertyRepository.findAllByHostId(host.getId()).size();
        long limit = getPropertyLimit(host.getSubscriptionTier());
        if (limit >= 0 && existingCount >= limit) {
            throw new IllegalStateException("Subscription tier limit reached for properties");
        }

        Property property = Property.builder()
                .hostId(host.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .wifiName(dto.getWifiName())
                .wifiPassword(dto.getWifiPassword())
                .airbnbReviewUrl(dto.getAirbnbReviewUrl())
                .bookingReviewUrl(dto.getBookingReviewUrl())
                .icalUrls(dto.getIcalUrls())
                .checkoutChecklist(dto.getCheckoutChecklist())
                .faqList(dto.getFaqList())
                .mapMarkers(dto.getMapMarkers())
                .customDomain(dto.getCustomDomain())
                .build();
        return propertyRepository.save(property);
    }

    public Property updateProperty(String hostEmail, String propertyId, PropertyDto dto) {
        Property existing = getOwnedProperty(hostEmail, propertyId);
        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        existing.setWifiName(dto.getWifiName());
        existing.setWifiPassword(dto.getWifiPassword());
        existing.setAirbnbReviewUrl(dto.getAirbnbReviewUrl());
        existing.setBookingReviewUrl(dto.getBookingReviewUrl());
        existing.setIcalUrls(dto.getIcalUrls());
        existing.setCheckoutChecklist(dto.getCheckoutChecklist());
        existing.setFaqList(dto.getFaqList());
        existing.setMapMarkers(dto.getMapMarkers());
        existing.setCustomDomain(dto.getCustomDomain());
        return propertyRepository.save(existing);
    }

    public void deleteProperty(String hostEmail, String propertyId) {
        Property property = getOwnedProperty(hostEmail, propertyId);
        propertyRepository.delete(property);
    }

    public Property getOwnedProperty(String hostEmail, String propertyId) {
        User host = getUserByEmail(hostEmail);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        if (!property.getHostId().equals(host.getId())) {
            throw new IllegalArgumentException("Property does not belong to authenticated host");
        }
        return property;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private long getPropertyLimit(User.SubscriptionTier tier) {
        if (tier == null || tier == User.SubscriptionTier.FREE) {
            return 1;
        }
        if (tier == User.SubscriptionTier.STANDARD) {
            return 3;
        }
        return -1;
    }
}
