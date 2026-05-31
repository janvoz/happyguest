package com.guesthost.dto;

import com.guesthost.model.Property;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PropertyDto {
    private String id;

    @NotBlank(message = "Property name is required")
    private String name;

    private String address;
    private String wifiName;
    private String wifiPassword;
    private String airbnbReviewUrl;
    private String bookingReviewUrl;
    private List<String> icalUrls = new ArrayList<>();
    private List<String> checkoutChecklist = new ArrayList<>();
    private List<Property.FaqItem> faqList = new ArrayList<>();
    private List<Property.MapMarker> mapMarkers = new ArrayList<>();
    private String customDomain;
}
