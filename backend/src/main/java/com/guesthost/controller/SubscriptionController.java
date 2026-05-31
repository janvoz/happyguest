package com.guesthost.controller;

import com.guesthost.model.User;
import com.guesthost.service.SubscriptionFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionFeatureService subscriptionFeatureService;

    @GetMapping("/api/host/subscription/features")
    public ResponseEntity<SubscriptionFeaturesResponse> getFeatures(Authentication authentication) {
        User.SubscriptionTier tier = subscriptionFeatureService.getTier(authentication.getName());
        List<String> features = subscriptionFeatureService.getFeatures(authentication.getName());
        return ResponseEntity.ok(new SubscriptionFeaturesResponse(tier.name(), features));
    }

    public record SubscriptionFeaturesResponse(String tier, List<String> features) {}
}
