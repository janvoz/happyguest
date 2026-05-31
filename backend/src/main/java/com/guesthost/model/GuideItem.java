package com.guesthost.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "guide_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideItem {

    @Id
    private String id;

    private String propertyId;
    private String title;
    private String titleCs;
    private String slug;
    private String contentMarkdown;
    private String videoUrl;
    private String qrCodeUrl;

    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    private int displayOrder;

    /** Czech-language version of the guide content (optional). */
    private String contentMarkdownCs;
}
