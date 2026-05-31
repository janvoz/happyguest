package com.guesthost.security;

import com.guesthost.service.SubscriptionFeatureService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureGateAspect {

    private final SubscriptionFeatureService subscriptionFeatureService;

    @Before("@within(requiresFeature) || @annotation(requiresFeature)")
    public void enforceFeature(JoinPoint joinPoint, RequiresFeature requiresFeature) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Missing authentication for feature access");
        }
        if (!subscriptionFeatureService.isEnabledForUser(authentication.getName(), requiresFeature.value())) {
            throw new AccessDeniedException("Feature not enabled: " + requiresFeature.value());
        }
    }
}
