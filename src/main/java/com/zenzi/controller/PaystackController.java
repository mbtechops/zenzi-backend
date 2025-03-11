package com.zenzi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenzi.dto.*;
import com.zenzi.service.PaystackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Paystack Controller")
public class PaystackController {

    private final PaystackService paystackService;

    @PostMapping("/createRecipient")
    @Operation(summary = "Create Recipient")
    public ResponseEntity<RecipientDTO> createRecipient(
            @RequestParam String name,
            @RequestParam String accountNumber,
            @RequestParam String bankCode,
            @RequestParam String currency
    ) throws IOException {

        String stringRecipient = paystackService.createRecipient(name, accountNumber, bankCode, currency);

        ObjectMapper mapper = new ObjectMapper();
        RecipientResponse recipient = mapper.readValue(stringRecipient, RecipientResponse.class);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(recipient.getData());
    }

    @PostMapping("/initiate-payout")
    @Operation(summary = "Initiate payout")
    public ResponseEntity<TransferResponse> initiatePayout(
            @RequestParam double amount,
            @RequestParam String recipientCode,
            @RequestParam String reason) {

        TransferResponse int_payOut = paystackService.initiatePayout(amount, recipientCode, reason);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(int_payOut);
    }

    @PostMapping("/finalize_payout")
    public ResponseEntity<String> finalizePayout(
            @RequestParam String transferCode,
            @RequestParam String otp) throws IOException {
        String response = paystackService.finalizePayout(transferCode, otp); //otp in the test domain is 123456

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/verify-payout")
    public ResponseEntity<String> verifyPayout(@RequestParam String transferIdOrCode) throws IOException {
        String verifyPayOut = paystackService.verifyPayout(transferIdOrCode);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(verifyPayOut);
    }

    @GetMapping("/get_balance")
    public ResponseEntity<String> checkBalance() throws IOException {
        String response = paystackService.checkBalance();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/initialize_payment")
    @Operation(summary = "Initialize payment")
    public ResponseEntity<String> initializeTransaction(
            @RequestParam double amount, @RequestParam String email) throws IOException {
        String stripeResponse = paystackService.initiatePayment(amount, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @GetMapping("/verify_payment")
    @Operation(summary = "Verify payment")
    public ResponseEntity<String> verifyTransaction(@RequestParam String reference) throws IOException {
        String stripeResponse = paystackService.verifyPayment(reference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook to listen to Paystack events")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("x-paystack-signature") String signature) {
        boolean isValid = paystackService.verifySignature(payload, signature);

        if (isValid) {
            System.out.println("Webhook received: " + payload);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Webhook processed successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Invalid webhook signature");
        }
    }

    @GetMapping("/get_banks")
    @Operation(summary = "Get banks details")
    public ResponseEntity<List<BankDTO>> getBanks() throws IOException {

        String banksJson = paystackService.getBanks();
//
        ObjectMapper mapper = new ObjectMapper();
        BankResponse banks = mapper.readValue(banksJson, BankResponse.class);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(banks.getData());
//
    }
}

