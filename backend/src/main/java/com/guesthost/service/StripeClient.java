package com.guesthost.service;

import com.guesthost.model.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class StripeClient {

    private final String secretKey;

    public StripeClient(@Value("${app.stripe.secret-key:}") String secretKey) {
        this.secretKey = secretKey;
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    public PaymentIntentResult createPaymentIntent(long amountCents, long applicationFeeAmountCents, String currency,
                                                   Map<String, String> metadata) {
        if (secretKey == null || secretKey.isBlank()) {
            String id = "pi_mock_" + UUID.randomUUID().toString().replace("-", "");
            return new PaymentIntentResult(id, "mock_client_secret_" + id);
        }
        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .setApplicationFeeAmount(applicationFeeAmountCents);
            metadata.forEach(builder::putMetadata);
            PaymentIntent paymentIntent = PaymentIntent.create(builder.build());
            return new PaymentIntentResult(paymentIntent.getId(), paymentIntent.getClientSecret());
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to create Stripe payment intent", ex);
        }
    }

    public CheckoutSessionResult createSubscriptionCheckoutSession(User user, User.SubscriptionTier tier,
                                                                   String successUrl, String cancelUrl) {
        if (secretKey == null || secretKey.isBlank()) {
            String customerId = user.getStripeAccountId() == null ? "cus_mock_" + UUID.randomUUID().toString().replace("-", "") : user.getStripeAccountId();
            String sessionId = "cs_mock_" + UUID.randomUUID().toString().replace("-", "");
            return new CheckoutSessionResult(sessionId, successUrl + "?session_id=" + sessionId, customerId);
        }
        try {
            String customerId = user.getStripeAccountId();
            if (customerId == null || customerId.isBlank()) {
                Customer customer = Customer.create(CustomerCreateParams.builder().setEmail(user.getEmail()).build());
                customerId = customer.getId();
            }
            long amount = switch (tier) {
                case STANDARD -> 4900L;
                case PRO -> 9900L;
                default -> throw new IllegalArgumentException("Free tier does not require subscription checkout");
            };
            com.stripe.param.checkout.SessionCreateParams params = com.stripe.param.checkout.SessionCreateParams.builder()
                    .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(user.getId())
                    .putMetadata("userId", user.getId())
                    .putMetadata("tier", tier.name())
                    .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(amount)
                                    .setRecurring(com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                            .setInterval(com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                            .build())
                                    .setProductData(ProductData.builder().setName("GuestHost " + tier.name() + " Plan").build())
                                    .build())
                            .build())
                    .build();
            Session session = Session.create(params);
            return new CheckoutSessionResult(session.getId(), session.getUrl(), customerId);
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to create Stripe checkout session", ex);
        }
    }

    public String createBillingPortalSession(String customerId, String returnUrl) {
        if (secretKey == null || secretKey.isBlank()) {
            return returnUrl + "?portal=mock";
        }
        try {
            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
                    SessionCreateParams.builder()
                            .setCustomer(customerId)
                            .setReturnUrl(returnUrl)
                            .build()
            );
            return session.getUrl();
        } catch (StripeException ex) {
            throw new IllegalStateException("Failed to create Stripe billing portal session", ex);
        }
    }

    public record PaymentIntentResult(String id, String clientSecret) {}
    public record CheckoutSessionResult(String sessionId, String url, String customerId) {}
}
