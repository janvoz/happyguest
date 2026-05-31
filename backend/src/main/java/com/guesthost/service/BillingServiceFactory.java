package com.guesthost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceFactory {

    private final List<BillingService> billingServices;

    public BillingService selectProvider(String billingProvider) {
        return billingServices.stream()
                .filter(service -> service.supports(billingProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported billing provider: " + billingProvider));
    }
}
