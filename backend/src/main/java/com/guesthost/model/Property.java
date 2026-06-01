package com.guesthost.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    private String id;

    @NotBlank(message = "Host id is required")
    private String hostId;

    @NotBlank(message = "Property name is required")
    private String name;

    private String address;
    private String wifiName;
    private String wifiPassword;
    private String airbnbReviewUrl;
    private String bookingReviewUrl;

    @Builder.Default
    private List<String> icalUrls = new ArrayList<>();

    @Builder.Default
    private List<String> checkoutChecklist = new ArrayList<>();

    @Builder.Default
    private List<FaqItem> faqList = new ArrayList<>();

    @Builder.Default
    private List<MapMarker> mapMarkers = new ArrayList<>();

    @Builder.Default
    private List<QuickContact> quickContacts = new ArrayList<>();

    private String customDomain;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaqItem {
        private String question;
        private String answer;
        /** Czech-language question (optional). */
        private String questionCs;
        /** Czech-language answer (optional). */
        private String answerCs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapMarker {
        private String title;
        private String category;
        private String description;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickContact {
        private String label;
        private String phone;
    }
}
