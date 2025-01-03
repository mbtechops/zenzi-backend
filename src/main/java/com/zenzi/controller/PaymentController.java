package com.zenzi.controller;

import com.zenzi.dto.BalanceDTO;
import com.zenzi.dto.ProductRequest;
import com.zenzi.dto.StripeResponse;
import com.zenzi.service.IStripeService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Stripe Controller")
public class PaymentController {

    private final IStripeService stripeService;

    @PostMapping("/checkout")
    @Operation(summary = "Create Checkout Session", description = "This endpoint creates a Stripe checkout session")
    public ResponseEntity<StripeResponse> createSession(@RequestBody ProductRequest productRequest) {
        StripeResponse stripeResponse = stripeService.createCheckoutSession(productRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @GetMapping("balance")
    @Operation(summary = "Get Balance", description = "This endpoint retrieves merchant balance on Stripe")
    public ResponseEntity<BalanceDTO> getBalance() throws StripeException {
        return ResponseEntity.status(HttpStatus.OK).body(stripeService.getBalance());
    }


    @PostMapping("/webhook")
    @Operation(summary = "Listen For Events", description = "Endpoint to listen for events from Stripe")
    public ResponseEntity<String> handleStripeWebhook(
            HttpServletRequest request
    ) throws IOException, EventDataObjectDeserializationException {

        return ResponseEntity.ok(stripeService.handleStripeWebhook(request));
    }
}
