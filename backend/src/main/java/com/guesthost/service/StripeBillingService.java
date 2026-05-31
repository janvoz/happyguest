package com.guesthost.service;

import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeBillingService implements BillingService {

    private final UserRepository userRepository;
    private final StripeClient stripeClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public boolean supports(String billingProvider) {
        return billingProvider == null || billingProvider.isBlank() || "STRIPE".equalsIgnoreCase(billingProvider);
    }

    @Override
    public String createSubscription(String userId, User.SubscriptionTier tier) {
        if (tier == null || tier == User.SubscriptionTier.FREE) {
            throw new IllegalArgumentException("Please choose a paid subscription tier");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        StripeClient.CheckoutSessionResult result = stripeClient.createSubscriptionCheckoutSession(
                user,
                tier,
                frontendUrl + "/billing/success",
                frontendUrl + "/billing/cancel"
        );
        user.setBillingProvider("STRIPE");
        user.setStripeAccountId(result.customerId());
        user.setSubscriptionId(result.sessionId());
        user.setSubscriptionTier(tier);
        user.setSubscriptionStatus("PENDING");
        userRepository.save(user);
        return result.url();
    }

    @Override
    public String createPortalSession(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStripeAccountId() == null || user.getStripeAccountId().isBlank()) {
            throw new IllegalStateException("Stripe customer has not been created for this user");
        }
        return stripeClient.createBillingPortalSession(user.getStripeAccountId(), frontendUrl + "/billing");
    }
}
