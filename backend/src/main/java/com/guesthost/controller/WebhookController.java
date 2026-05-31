package com.guesthost.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import com.guesthost.service.OrderProcessingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final UserRepository userRepository;
    private final OrderProcessingService orderProcessingService;
    private final ObjectMapper objectMapper;

    @Value("${app.stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.fastspring.webhook-secret:}")
    private String fastSpringWebhookSecret;

    @PostMapping("/api/webhooks/stripe")
    public ResponseEntity<Map<String, Object>> handleStripeWebhook(@RequestBody String payload,
                                                                   @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if ("customer.subscription.updated".equals(event.getType()) && stripeObject instanceof Subscription subscription) {
                handleSubscriptionUpdated(subscription);
            } else if ("payment_intent.succeeded".equals(event.getType()) && stripeObject instanceof PaymentIntent paymentIntent) {
                orderProcessingService.confirmOrder(paymentIntent.getId());
            }
            return ResponseEntity.ok(Map.of("received", true, "type", event.getType()));
        } catch (SignatureVerificationException ex) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }
    }

    @PostMapping("/api/webhooks/fastspring")
    public ResponseEntity<Map<String, Object>> handleFastSpringWebhook(@RequestBody String payload,
                                                                       @RequestHeader(value = "X-FS-Signature", required = false) String signature) throws Exception {
        verifyFastSpringSignature(payload, signature);
        JsonNode root = objectMapper.readTree(payload);
        JsonNode eventsNode = root.path("events");
        if (eventsNode.isArray()) {
            for (JsonNode event : eventsNode) {
                processFastSpringEvent(event);
            }
        }
        return ResponseEntity.ok(Map.of("received", true, "provider", "fastspring"));
    }

    private void verifyFastSpringSignature(String payload, String signature) {
        if (fastSpringWebhookSecret == null || fastSpringWebhookSecret.isBlank()) {
            return;
        }
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing FastSpring signature");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(fastSpringWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid FastSpring webhook signature");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 for FastSpring signature", e);
        }
    }

    private void processFastSpringEvent(JsonNode event) {
        String type = event.path("type").asText("");
        JsonNode data = event.path("data");
        String subscriptionId = data.path("id").asText(null);
        switch (type) {
            case "subscription.activated" -> handleFastSpringSubscriptionActivated(data, subscriptionId);
            case "subscription.deactivated", "subscription.canceled" -> handleFastSpringSubscriptionDeactivated(subscriptionId);
            default -> log.debug("Unhandled FastSpring event type: {}", type);
        }
    }

    private void handleFastSpringSubscriptionActivated(JsonNode data, String subscriptionId) {
        User user = resolveUserForFastSpring(data, subscriptionId);
        if (user == null) {
            return;
        }
        String sku = data.path("sku").asText("").toUpperCase(Locale.ROOT);
        User.SubscriptionTier tier = parseFastSpringTier(sku);
        user.setSubscriptionId(subscriptionId);
        user.setSubscriptionStatus("active");
        user.setBillingProvider("FASTSPRING");
        if (tier != null) {
            user.setSubscriptionTier(tier);
        }
        userRepository.save(user);
    }

    private void handleFastSpringSubscriptionDeactivated(String subscriptionId) {
        if (subscriptionId == null) {
            return;
        }
        userRepository.findBySubscriptionId(subscriptionId).ifPresent(user -> {
            user.setSubscriptionStatus("canceled");
            userRepository.save(user);
        });
    }

    private User resolveUserForFastSpring(JsonNode data, String subscriptionId) {
        if (subscriptionId != null) {
            Optional<User> bySubId = userRepository.findBySubscriptionId(subscriptionId);
            if (bySubId.isPresent()) {
                return bySubId.get();
            }
        }
        String email = data.path("account").path("contact").path("email").asText(null);
        if (email == null || email.isBlank()) {
            email = data.path("email").asText(null);
        }
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmail(email).orElse(null);
        }
        log.warn("Cannot resolve user for FastSpring subscription event, subscriptionId={}", subscriptionId);
        return null;
    }

    private User.SubscriptionTier parseFastSpringTier(String sku) {
        for (User.SubscriptionTier tier : User.SubscriptionTier.values()) {
            if (sku.contains(tier.name())) {
                return tier;
            }
        }
        return null;
    }

    private void handleSubscriptionUpdated(Subscription subscription) {
        User user = userRepository.findByStripeAccountId(subscription.getCustomer())
                .or(() -> userRepository.findBySubscriptionId(subscription.getId()))
                .orElse(null);
        if (user == null) {
            log.warn("No user matched Stripe subscription update for customer {}", subscription.getCustomer());
            return;
        }
        user.setSubscriptionId(subscription.getId());
        user.setSubscriptionStatus(subscription.getStatus());
        user.setBillingProvider("STRIPE");
        String tierMetadata = subscription.getMetadata() == null ? null : subscription.getMetadata().get("tier");
        if (tierMetadata != null && !tierMetadata.isBlank()) {
            user.setSubscriptionTier(User.SubscriptionTier.valueOf(tierMetadata.toUpperCase(Locale.ROOT)));
        }
        userRepository.save(user);
    }
}
