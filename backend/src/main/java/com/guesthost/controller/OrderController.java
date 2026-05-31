package com.guesthost.controller;

import com.guesthost.dto.MinibarOrderRequest;
import com.guesthost.model.MinibarOrder;
import com.guesthost.service.OrderProcessingService;
import com.guesthost.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class OrderController {

    private final OrderProcessingService orderProcessingService;
    private final PropertyService propertyService;

    @PostMapping("/api/public/guest/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody MinibarOrderRequest request) {
        OrderProcessingService.OrderResult result = orderProcessingService.createOrder(request);
        return ResponseEntity.ok(Map.of(
                "order", result.order(),
                "clientSecret", result.clientSecret()
        ));
    }

    @GetMapping("/api/host/orders/{propertyId}")
    public ResponseEntity<List<MinibarOrder>> getOrders(Authentication authentication, @PathVariable String propertyId) {
        return ResponseEntity.ok(orderProcessingService.getOrdersForProperty(authentication.getName(), propertyId, propertyService));
    }
}
