package com.guesthost.service;

import com.guesthost.dto.GuideItemDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.GuideItem;
import com.guesthost.model.Property;
import com.guesthost.repository.GuideItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final GuideItemRepository guideItemRepository;
    private final PropertyService propertyService;
    private final TranslationService translationService;

    public List<GuideItem> getGuidesForHost(String hostEmail, String propertyId) {
        propertyService.getOwnedProperty(hostEmail, propertyId);
        return guideItemRepository.findAllByPropertyIdOrderByDisplayOrder(propertyId);
    }

    public List<GuideItem> getPublicGuides(String propertyId) {
        return guideItemRepository.findAllByPropertyIdOrderByDisplayOrder(propertyId);
    }

    public GuideItem getPublicGuideBySlug(String propertyId, String slug) {
        return guideItemRepository.findByPropertyIdAndSlug(propertyId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found: " + slug));
    }

    public List<GuideItem> getPublicGuides(String propertyId, String language) {
        return guideItemRepository.findAllByPropertyIdOrderByDisplayOrder(propertyId).stream()
                .map(item -> localize(item, language))
                .toList();
    }

    public GuideItem getPublicGuideBySlug(String propertyId, String slug, String language) {
        GuideItem item = guideItemRepository.findByPropertyIdAndSlug(propertyId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found: " + slug));
        return localize(item, language);
    }

    public GuideItem createGuide(String hostEmail, GuideItemDto dto) {
        Property property = propertyService.getOwnedProperty(hostEmail, dto.getPropertyId());
        String slug = ensureUniqueSlug(property.getId(), dto.getSlug(), dto.getTitle(), null);
        GuideItem guideItem = GuideItem.builder()
                .propertyId(property.getId())
                .title(dto.getTitle())
                .titleCs(dto.getTitleCs())
                .slug(slug)
                .contentMarkdown(dto.getContentMarkdown())
                .contentMarkdownCs(dto.getContentMarkdownCs())
                .videoUrl(dto.getVideoUrl())
                .qrCodeUrl(buildQrCodeUrl(property.getId(), slug))
                .photoUrls(dto.getPhotoUrls())
                .displayOrder(dto.getDisplayOrder())
                .build();
        return guideItemRepository.save(guideItem);
    }

    public GuideItem updateGuide(String hostEmail, String guideId, GuideItemDto dto) {
        GuideItem existing = guideItemRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide item not found: " + guideId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        String slug = ensureUniqueSlug(existing.getPropertyId(), dto.getSlug(), dto.getTitle(), existing.getId());
        existing.setTitle(dto.getTitle());
        existing.setTitleCs(dto.getTitleCs());
        existing.setSlug(slug);
        existing.setContentMarkdown(dto.getContentMarkdown());
        existing.setContentMarkdownCs(dto.getContentMarkdownCs());
        existing.setVideoUrl(dto.getVideoUrl());
        existing.setPhotoUrls(dto.getPhotoUrls());
        existing.setDisplayOrder(dto.getDisplayOrder());
        existing.setQrCodeUrl(buildQrCodeUrl(existing.getPropertyId(), slug));
        return guideItemRepository.save(existing);
    }

    public void deleteGuide(String hostEmail, String guideId) {
        GuideItem existing = guideItemRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide item not found: " + guideId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        guideItemRepository.delete(existing);
    }

    private String ensureUniqueSlug(String propertyId, String requestedSlug, String title, String currentId) {
        String baseSlug = slugify((requestedSlug == null || requestedSlug.isBlank()) ? title : requestedSlug);
        String slug = baseSlug;
        int index = 1;
        while (true) {
            GuideItem existing = guideItemRepository.findByPropertyIdAndSlug(propertyId, slug).orElse(null);
            if (existing == null || existing.getId().equals(currentId)) {
                return slug;
            }
            slug = baseSlug + "-" + index++;
        }
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "guide-item" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String buildQrCodeUrl(String propertyId, String slug) {
        return "/api/public/qr/" + propertyId + "/" + slug;
    }

    private GuideItem localize(GuideItem item, String language) {
        if (language == null || language.isBlank() || "en".equalsIgnoreCase(language)) {
            return item;
        }
        if (!"cs".equalsIgnoreCase(language)) {
            return GuideItem.builder()
                    .id(item.getId())
                    .propertyId(item.getPropertyId())
                    .title(translationService.translate(item.getTitle(), language))
                    .titleCs(item.getTitleCs())
                    .slug(item.getSlug())
                    .contentMarkdown(translationService.translate(item.getContentMarkdown(), language))
                    .contentMarkdownCs(item.getContentMarkdownCs())
                    .videoUrl(item.getVideoUrl())
                    .qrCodeUrl(item.getQrCodeUrl())
                    .photoUrls(item.getPhotoUrls())
                    .displayOrder(item.getDisplayOrder())
                    .build();
        }
        GuideItem localized = GuideItem.builder()
                .id(item.getId())
                .propertyId(item.getPropertyId())
                .title((item.getTitleCs() != null && !item.getTitleCs().isBlank()) ? item.getTitleCs() : item.getTitle())
                .titleCs(item.getTitleCs())
                .slug(item.getSlug())
                .contentMarkdown((item.getContentMarkdownCs() != null && !item.getContentMarkdownCs().isBlank())
                        ? item.getContentMarkdownCs()
                        : item.getContentMarkdown())
                .contentMarkdownCs(item.getContentMarkdownCs())
                .videoUrl(item.getVideoUrl())
                .qrCodeUrl(item.getQrCodeUrl())
                .photoUrls(item.getPhotoUrls())
                .displayOrder(item.getDisplayOrder())
                .build();
        return localized;
    }
}
