package com.zenzi.controller;

import com.zenzi.dto.response.paystack.*;
import com.zenzi.service.PaystackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Paystack Controller")
public class PaystackController {

    private final PaystackService paystackService;

    //    @PostMapping("/createRecipient") NO LONGER NEEDED
    @Operation(summary = "Create Recipient")
    public ResponseEntity<TransferRecipientResponse> createRecipient(
            @RequestParam String name,
            @RequestParam String accountNumber,
            @RequestParam String bankCode,
            @RequestParam String currency
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(paystackService.createRecipient(name, accountNumber, bankCode, currency));
    }

    @PostMapping("/initiate-payout")
    @Operation(summary = "Initiate payout")
    public ResponseEntity<TransferResponse> initiatePayout(
            @RequestParam String name,
            @RequestParam String accountNumber,
            @RequestParam String bankCode,
            @RequestParam String currency,
            @RequestParam double amount,
            @RequestParam String reason) {

        TransferResponse int_payOut = paystackService.initiatePayout(name, accountNumber, bankCode, currency, amount, reason);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(int_payOut);
    }

    //    @PostMapping("/finalize_payout") NO LONGER NEEDED
    public ResponseEntity<FinalizePayoutResponse> finalizePayout(
            @RequestParam String transferCode,
            @RequestParam String otp) throws IOException {
        FinalizePayoutResponse response = paystackService.finalizePayout(transferCode, otp); //otp in the test domain is 123456

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/verify-payout")
    public ResponseEntity<VerifyPaymentResponse> verifyPayout(@RequestParam String transferIdOrCode) throws IOException {
        VerifyPaymentResponse verifyPayOut = paystackService.verifyPayout(transferIdOrCode);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(verifyPayOut);
    }

    @GetMapping("/get_balance")
    public ResponseEntity<BalanceResponse> checkBalance() throws IOException {
        BalanceResponse response = paystackService.checkBalance();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/initialize_payment")
    @Operation(summary = "Initialize payment")
    public ResponseEntity<TransactionResponse> initializeTransaction(
            @RequestParam double amount, @RequestParam String email) throws IOException {
        TransactionResponse stripeResponse = paystackService.initiatePayment(amount, email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @GetMapping("/verify_payment")
    @Operation(summary = "Verify payment")
    public ResponseEntity<VerifyPaymentResponse> verifyTransaction(@RequestParam String reference) throws IOException {
        VerifyPaymentResponse stripeResponse = paystackService.verifyPayment(reference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

    @GetMapping("/get_banks")
    @Operation(summary = "Get banks details")
    public ResponseEntity<BankResponse> getBanks() throws IOException {

        BankResponse banksJson = paystackService.getBanks();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(banksJson);
    }

    @PostMapping("/webhook") // after using ngrok, open this "http://localhost:4040" on your browser to get the event
    public ResponseEntity<WebhookResponse> listenToEvents(
            @RequestBody String rawPayload,
            @RequestHeader("x-paystack-signature") String signature) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(paystackService.handleWebhook(rawPayload, signature));
    }
}

