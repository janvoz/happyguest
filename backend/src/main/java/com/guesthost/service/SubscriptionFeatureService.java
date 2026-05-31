package com.guesthost.service;

import com.guesthost.config.SubscriptionFeatureProperties;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionFeatureService {

    private final UserRepository userRepository;
    private final SubscriptionFeatureProperties subscriptionFeatureProperties;

    public boolean isEnabledForUser(String hostEmail, String featureId) {
        User user = getUserByEmail(hostEmail);
        return subscriptionFeatureProperties.getFeaturesForTier(user.getSubscriptionTier()).contains(featureId);
    }

    public User.SubscriptionTier getTier(String hostEmail) {
        return getUserByEmail(hostEmail).getSubscriptionTier();
    }

    public List<String> getFeatures(String hostEmail) {
        return subscriptionFeatureProperties.getFeaturesForTier(getTier(hostEmail));
    }

    private User getUserByEmail(String hostEmail) {
        return userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + hostEmail));
    }
}
