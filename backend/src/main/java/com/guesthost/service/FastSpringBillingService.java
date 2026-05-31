package com.guesthost.service;

import com.guesthost.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FastSpringBillingService implements BillingService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public boolean supports(String billingProvider) {
        return "FASTSPRING".equalsIgnoreCase(billingProvider);
    }

    @Override
    public String createSubscription(String userId, User.SubscriptionTier tier) {
        log.info("FastSpring billing not fully implemented for user {} tier {}", userId, tier);
        return frontendUrl + "/billing/fastspring?userId=" + userId + "&tier=" + tier;
    }

    @Override
    public String createPortalSession(String userId) {
        log.info("FastSpring billing not fully implemented for user {}", userId);
        return frontendUrl + "/billing/fastspring/portal?userId=" + userId;
    }
}
