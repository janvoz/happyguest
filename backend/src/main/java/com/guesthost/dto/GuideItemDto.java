package com.guesthost.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GuideItemDto {
    private String id;

    @NotBlank(message = "Property id is required")
    private String propertyId;

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;
    private String contentMarkdown;
    private String videoUrl;
    private List<String> photoUrls = new ArrayList<>();
    private int displayOrder;
}
