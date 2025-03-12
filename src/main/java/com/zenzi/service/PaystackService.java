package com.zenzi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenzi.configurations.PaystackConfig;
import com.zenzi.dto.response.paystack.*;
import com.zenzi.exceptions.PaystackException;
import lombok.AllArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


@Service
@AllArgsConstructor
public class PaystackService {

    private final PaystackConfig paystackConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // to simulate funding the balance by initiating a transaction
    public TransactionResponse initiatePayment(double amount, String email) {
        String requestBody = String.format("{\"amount\": %.0f, \"email\": \"%s\"}", amount * 100, email);

        return webClient.post()
                .uri("/transaction/initialize")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .block();
    }


    public VerifyPaymentResponse verifyPayment(String reference) {
        return webClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .retrieve()
                .bodyToMono(VerifyPaymentResponse.class)
                .block();
    }

    public BalanceResponse checkBalance() {

        return webClient.get()
                .uri("/balance")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .block();
    }


    public TransferRecipientResponse createRecipient(String name, String accountNumber, String bankCode, String currency) {
        String requestBody = String.format("{\"type\": \"nuban\", \"name\": \"%s\", \"account_number\": \"%s\", \"bank_code\": \"%s\", \"currency\": \"%s\"}",
                name, accountNumber, bankCode, currency);

        return webClient.post()
                .uri("/transferrecipient")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TransferRecipientResponse.class)
                .block();
    }

    public TransferResponse initiatePayout(
            String name,
            String accountNumber,
            String bankCode,
            String currency,
            double amount,
            String reason
    ) {
        String recipientCode = null;

        try {
            TransferRecipientResponse response = createRecipient(name, accountNumber, bankCode, currency);

            if (response.isStatus()) {
                recipientCode = response.getData().getRecipient_code();
            }

        } catch (RuntimeException e) {
            throw new PaystackException(e.getMessage());
        }

        String requestBody = String.format(
                "{\"source\": \"balance\", \"amount\":  %.0f, \"recipient\": \"%s\", \"reason\": \"%s\"}",
                amount * 100, recipientCode, reason);

        return webClient.post()
                .uri("/transfer")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TransferResponse.class)
                .block();
    }


//    public TransferResponse initiatePayout(double amount, String recipientCode, String reason) {
//        String requestBody = String.format(
//                "{\"source\": \"balance\", \"amount\":  %.0f, \"recipient\": \"%s\", \"reason\": \"%s\"}",
//                amount * 100, recipientCode, reason);
//
//        return webClient.post()
//                .uri("/transfer")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(TransferResponse.class)
//                .block();
//    }

    public FinalizePayoutResponse finalizePayout(String transferCode, String otp) throws IOException {
        String url = paystackConfig.getBaseUrl() + "/transfer/finalize_transfer";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());
            post.setHeader("Content-Type", "application/json");

            String json = String.format(
                    "{\"transfer_code\": \"%s\", \"otp\": \"%s\"}",
                    transferCode, otp);
            post.setEntity(new StringEntity(json));

            HttpResponse response = client.execute(post);

//            ObjectMapper mapper = new ObjectMapper();

            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), FinalizePayoutResponse.class);
        }
    }

    public VerifyPaymentResponse verifyPayout(String transferIdOrCode) {
        return webClient.get()
                .uri("/transfer/{transferIdOrCode}", transferIdOrCode)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .retrieve()
                .bodyToMono(VerifyPaymentResponse.class)
                .block(); // Synchronous call to match original behavior
    }

    public BankResponse getBanks() {
        return webClient.get()
                .uri("/bank")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + paystackConfig.getSecretKey())
                .retrieve()
                .bodyToMono(BankResponse.class)
                .block(); // Synchronous call to match original behavior
    }


    public WebhookResponse handleWebhook(String rawPayload, String signature) {

        // to verify if the event came from paystack
        if (!verifySignature(rawPayload, signature)) {
            return WebhookResponse.builder().message("Invalid signature").build();
        }

        try {

            WebhookEvent event = parseWebhookPayload(rawPayload);

            switch (event.getEvent()) {

                case "transfer.success" -> {
                    handlePayoutCompletion(event.getData());
                    return WebhookResponse.builder().message("Webhook processed").build();
                }

                case "transfer.failed" -> {
                    // if transfer failed, we handle the failure here
                    handlePayoutFailure(event.getData());
                    return WebhookResponse.builder().message("Webhook processed").build();
                }

                default -> {
                    return WebhookResponse.builder().message("Unknown Event!").build();
                }
            }

        } catch (Exception e) {
            return WebhookResponse.builder()
                    .message("Error processing webhook: " + e.getMessage())
                    .build();
        }
    }


    private boolean verifySignature(String payload, String signature) {
        try {
            String secretKey = paystackConfig.getSecretKey();

            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secretKeySpec);
            byte[] hashBytes = sha512_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // to convert to hex instead of Base64
            String computedSignature = HexFormat.of().formatHex(hashBytes);

            return computedSignature.equals(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new PaystackException(e.getMessage());
        }
    }

    public WebhookEvent parseWebhookPayload(String rawPayload) throws Exception {
        return objectMapper.readValue(rawPayload, WebhookEvent.class);
    }


    public void handlePayoutCompletion(WebhookData data) {
        System.out.println("Payout completed: Transfer " + data.getTransfer_code() +
                " for " + data.getAmount() + " " + data.getCurrency() +
                " to recipient " + data.getRecipient().getRecipient_code() +
                " is " + data.getStatus());
    }

    public void handlePayoutFailure(WebhookData data) {
        System.out.println("Payout failed: Transfer " + data.getTransfer_code() +
                " - Status: " + data.getStatus());
    }
}