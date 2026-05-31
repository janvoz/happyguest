package com.guesthost.controller;

import com.guesthost.dto.MinibarItemDto;
import com.guesthost.model.MinibarItem;
import com.guesthost.service.MinibarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MinibarController {

    private final MinibarService minibarService;

    @GetMapping("/api/host/properties/{propertyId}/minibar-items")
    public ResponseEntity<List<MinibarItem>> getItemsByProperty(Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(minibarService.getItemsForHost(authentication.getName(), propertyId));
    }

    @GetMapping("/api/host/minibar")
    public ResponseEntity<List<MinibarItem>> getItems(Authentication authentication, @RequestParam String propertyId) {
        return ResponseEntity.ok(minibarService.getItemsForHost(authentication.getName(), propertyId));
    }

    @PostMapping({"/api/host/minibar-items", "/api/host/minibar"})
    public ResponseEntity<MinibarItem> createItem(Authentication authentication, @Valid @RequestBody MinibarItemDto dto) {
        return ResponseEntity.ok(minibarService.createItem(authentication.getName(), dto));
    }

    @PutMapping({"/api/host/minibar-items/{id}", "/api/host/minibar/{id}"})
    public ResponseEntity<MinibarItem> updateItem(Authentication authentication, @PathVariable String id,
                                                  @Valid @RequestBody MinibarItemDto dto) {
        return ResponseEntity.ok(minibarService.updateItem(authentication.getName(), id, dto));
    }

    @DeleteMapping({"/api/host/minibar-items/{id}", "/api/host/minibar/{id}"})
    public ResponseEntity<Void> deleteItem(Authentication authentication, @PathVariable String id) {
        minibarService.deleteItem(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/public/properties/{propertyId}/minibar-items")
    public ResponseEntity<List<MinibarItem>> getPublicItems(@PathVariable String propertyId) {
        return ResponseEntity.ok(minibarService.getPublicItems(propertyId));
    }
}
