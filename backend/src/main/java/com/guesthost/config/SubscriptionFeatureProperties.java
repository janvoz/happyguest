package com.guesthost.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.guesthost.model.User;

@Data
@Component
@ConfigurationProperties(prefix = "app.subscription")
public class SubscriptionFeatureProperties {
    private Map<User.SubscriptionTier, List<String>> tiers = new EnumMap<>(User.SubscriptionTier.class);

    public List<String> getFeaturesForTier(User.SubscriptionTier tier) {
        if (tier == null) {
            return tiers.getOrDefault(User.SubscriptionTier.FREE, new ArrayList<>());
        }
        return tiers.getOrDefault(tier, new ArrayList<>());
    }
}
