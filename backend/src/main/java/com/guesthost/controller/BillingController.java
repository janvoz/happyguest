package com.guesthost.controller;

import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import com.guesthost.service.BillingService;
import com.guesthost.service.BillingServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/host/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingServiceFactory billingServiceFactory;
    private final UserRepository userRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(Authentication authentication, @RequestBody Map<String, String> request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        User.SubscriptionTier tier = User.SubscriptionTier.valueOf(request.getOrDefault("tier", "FREE").toUpperCase());
        String provider = request.getOrDefault("billingProvider", user.getBillingProvider());
        BillingService billingService = billingServiceFactory.selectProvider(provider);
        String url = billingService.createSubscription(user.getId(), tier);
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    @GetMapping("/portal")
    public ResponseEntity<Map<String, String>> portal(Authentication authentication,
                                                      @RequestParam(required = false) String billingProvider) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        BillingService billingService = billingServiceFactory.selectProvider(
                billingProvider == null ? user.getBillingProvider() : billingProvider
        );
        return ResponseEntity.ok(Map.of("url", billingService.createPortalSession(user.getId())));
    }
}
