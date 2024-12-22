package com.zenzi.service;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenzi.dto.BalanceDTO;
import com.zenzi.dto.ProductRequest;
import com.zenzi.dto.StripeResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class StripeService {
//
//    @Value("${stripe.secretKey}")
//    private String secretKey;

    @Value("${stripe.endpointSecret}")
    private String stripeEndpointSecret;


    @Value("${stripe.successUrl}")
    private String successUrl;

    @Value("${stripe.cancelUrl}")
    private String cancelUrl;

    @Value("${app.name}")
    private String appName;


    public StripeResponse createCheckoutSession(ProductRequest productRequest) {
        // Set the Stripe API key
//        Stripe.apiKey = secretKey;

        // Determine currency, defaulting to USD
        final String CURRENCY = Optional.ofNullable(productRequest.getCurrency()).orElse("USD");

        // Build the session create parameters
        SessionCreateParams params = buildSessionCreateParams(productRequest, CURRENCY);

        // Configure idempotency to avoid duplicate requests
        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey())
                .build();

        try {
            Session session = Session.create(params, options);

            return StripeResponse.builder()
                    .status(String.valueOf(HttpStatus.CREATED))
                    .message("Payment session created successfully")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {

            log.error("StripeException occurred: {}", e.getMessage(), e);

            return StripeResponse.builder()
                    .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
                    .message(e.getMessage())
                    .build();
        }
    }

    private SessionCreateParams buildSessionCreateParams(ProductRequest productRequest, final String CURRENCY) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
//                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.PAYPAL)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(buildLineItem(productRequest, CURRENCY))
                .build();
    }

    private SessionCreateParams.LineItem buildLineItem(ProductRequest productRequest, final String CURRENCY) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY)
                                .setUnitAmount(productRequest.getAmount() * 100)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(appName)
                                                .build()
                                ).build()
                ).build();
    }

    public BalanceDTO getBalance() throws StripeException {
//        Stripe.apiKey = secretKey;

        Balance balance = Balance.retrieve();
        return BalanceDTO.builder()
                .currency(balance.getAvailable().getFirst().getCurrency())
                .availableAmount(balance.getAvailable().getFirst().getAmount())
                .liveMode(balance.getLivemode())
                .build();
    }


    public String handleStripeWebhook(HttpServletRequest request) throws IOException, EventDataObjectDeserializationException {

        String endpointSecret = stripeEndpointSecret; // Replace with your actual Stripe secret
        String sigHeader = request.getHeader("Stripe-Signature");
        String payload = getRequestBodyAsString(request);
        Event event;


        try {
            // Construct the event from the payload and signature
            event = ApiResource.GSON.fromJson(payload, Event.class);
            log.info("Event: {}", event.getType());

        } catch (Exception e) {
            log.error("Error while processing webhook: {}", e.getMessage());
            return "Invalid Payload";
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        StripeObject stripeObject = dataObjectDeserializer.deserializeUnsafe();

        String eventId = event.getId();

        // Process the event based on its type
        switch (event.getType()) {

            case "charge.captured" -> {
                if (stripeObject instanceof Charge charge) {
                    log.info("Charge Captured Object: {}", charge.getObject());


                    /*amount
                    amount_captured
                    * billing_details{address{country}, email, name, phone}
                    captured
                    currency
                    outcome{seller_message}
                    paid
                    card {amount_authorized, brand, country}
                    type
                    receipt_url
                    status


                    * */

                } else {
                    failedToDeserialized(eventId);
                }
            }

            case "charge.failed" -> {
                if (stripeObject instanceof Charge charge) {
                    log.info("Charge Failed Object: {}", charge.getObject());
                } else {
                    failedToDeserialized(eventId);
                }
            }

            case "charge.succeeded" -> {
                if (stripeObject instanceof Charge charge) {
                    log.info("""
                            Event Type: {}
                            Amount Charged: {}
                            Amount Captured: {}
                            User Country: {}
                            User Email: {}
                            User Name: {}
                            User Phone: {}
                            Captured: {}
                            Currency: {}
                            Message: {}
                            Paid: {}
                            Amount Authorized: {}
                            Card Brand: {}
                            Country of Issuance: {}
                            Payment Type: {}
                            Receipt URL: {}
                            Payment Status: {}
                            """,
                            event.getType(),
                            charge.getAmount(),
                            charge.getAmountCaptured(),
                            charge.getBillingDetails().getAddress().getCountry(),
                            charge.getBillingDetails().getEmail(),
                            charge.getBillingDetails().getName(),
                            charge.getBillingDetails().getPhone(),
                            charge.getCaptured(),
                            charge.getCurrency(),
                            charge.getOutcome().getSellerMessage(),
                            charge.getPaid(),
                            charge.getPaymentMethodDetails().getCard().getAmountAuthorized(),
                            charge.getPaymentMethodDetails().getCard().getBrand(),
                            charge.getPaymentMethodDetails().getCard().getCountry(),
                            charge.getPaymentMethodDetails().getType(),
                            charge.getReceiptUrl(),
                            charge.getStatus()
                    );
                } else {
                    failedToDeserialized(eventId);
                }
            }

            case "checkout.session.completed" -> {
                if (stripeObject instanceof Session session) {
                    log.info("Checkout Session Completed: {}", session.getObject());
                } else {
                    failedToDeserialized(eventId);
                }
            }

            case "balance.available" -> {
                if (stripeObject instanceof Balance balance) {
                    log.info("Balance Available Object: {}", balance.getObject());
                } else {
                    failedToDeserialized(eventId);
                }
            }

            default -> log.warn("Unhandled event type: {}", event.getType());
        }
        return HttpStatus.OK.toString();
    }

    private void failedToDeserialized(String eventId) {
        log.error("Failed to deserialize Customer object for event ID: {}", eventId);
    }

    public String getRequestBodyAsString(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

    private static String idempotencyKey() {
        long timestamp = System.currentTimeMillis() % 1_000_000_000; // Use only the last 9 digits of the timestamp
        String shortUUID = UUID.randomUUID().toString().replace("-", "").substring(0, 8); // Single UUID (8 chars)
        return String.format("%s%09d", shortUUID, timestamp); // Combine short UUID and timestamp
    }
}