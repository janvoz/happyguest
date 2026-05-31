package com.guesthost.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guesthost.model.MinibarOrder;
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

import java.util.Locale;
import java.util.Map;

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
        if (fastSpringWebhookSecret != null && !fastSpringWebhookSecret.isBlank() && (signature == null || signature.isBlank())) {
            throw new IllegalArgumentException("Missing FastSpring signature");
        }
        JsonNode root = objectMapper.readTree(payload);
        log.info("FastSpring billing not fully implemented. Signature present: {}, payload type: {}", signature != null, root.path("type").asText("unknown"));
        return ResponseEntity.ok(Map.of("received", true, "provider", "fastspring"));
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
