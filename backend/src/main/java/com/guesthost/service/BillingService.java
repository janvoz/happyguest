package com.guesthost.service;

import com.guesthost.model.User;

public interface BillingService {

    boolean supports(String billingProvider);

    String createSubscription(String userId, User.SubscriptionTier tier);

    String createPortalSession(String userId);
}
