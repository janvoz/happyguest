package com.guesthost.controller;

import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.GuideItem;
import com.guesthost.repository.GuideItemRepository;
import com.guesthost.service.GuideMediaService;
import com.guesthost.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MediaUploadController {

    private final GuideMediaService guideMediaService;
    private final GuideItemRepository guideItemRepository;
    private final PropertyService propertyService;

    /**
     * Upload a media file (image, PDF, video) and associate it with a guide item.
     * Returns the accessible URL for the stored file.
     */
    @PostMapping("/api/host/guides/{guideId}/media")
    public ResponseEntity<Map<String, String>> uploadGuideMedia(
            Authentication authentication,
            @PathVariable String guideId,
            @RequestParam("file") MultipartFile file) throws IOException {
        GuideItem guide = guideItemRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found: " + guideId));
        propertyService.getOwnedProperty(authentication.getName(), guide.getPropertyId());

        String fileId = guideMediaService.store(file);
        String url = "/api/public/media/" + fileId;

        // Append the new URL to the guide's photoUrls list
        List<String> photos = new ArrayList<>(guide.getPhotoUrls());
        photos.add(url);
        guide.setPhotoUrls(photos);
        guideItemRepository.save(guide);

        return ResponseEntity.ok(Map.of("url", url, "fileId", fileId));
    }

    /** Public endpoint to serve guide media files stored in GridFS. */
    @GetMapping("/api/public/media/{fileId}")
    public ResponseEntity<byte[]> serveMedia(@PathVariable String fileId) throws IOException {
        GuideMediaService.GridFsResource resource = guideMediaService.load(fileId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = StreamUtils.copyToByteArray(resource.resource().getInputStream());
        String contentType = resource.contentType();
        if (contentType == null || contentType.isBlank() || contentType.equals("null")) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(bytes);
    }
}
