package com.zenzi.service;

import com.zenzi.configurations.PaystackConfig;
import com.zenzi.dto.TransferResponse;
import lombok.AllArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;




@Service
@AllArgsConstructor
public class PaystackService {

    private final PaystackConfig paystackConfig;
    private final WebClient webClient;

    // to simulate funding the balance by initiating a transaction
    public String initiatePayment(double amount, String email) throws IOException {
        String url = paystackConfig.getBaseUrl() + "/transaction/initialize";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());
            post.setHeader("Content-Type", "application/json");

            String json = String.format("{\"amount\": %.0f, \"email\": \"%s\"}", amount * 100, email);
            post.setEntity(new StringEntity(json));

            HttpResponse response = client.execute(post);
            return EntityUtils.toString(response.getEntity());
        }
    }



    public String verifyPayment(String reference) throws IOException {
        String url = paystackConfig.getBaseUrl() + "/transaction/verify/" + reference;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());

            HttpResponse response = client.execute(get);
            return EntityUtils.toString(response.getEntity());

        }
    }

    public String checkBalance() throws IOException {
        String url = paystackConfig.getBaseUrl() + "/balance";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());

            HttpResponse response = client.execute(get);

            return EntityUtils.toString(response.getEntity());

        }
    }

    public boolean verifySignature(String payload, String signature) {
        try {
            String secretKey = "your_paystack_secret_key"; // Replace with your secret key
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA512");
            sha512_HMAC.init(secretKeySpec);

            byte[] hashBytes = sha512_HMAC.doFinal(payload.getBytes());
            String computedSignature = Base64.getEncoder().encodeToString(hashBytes);

            return computedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String createRecipient(String name, String accountNumber, String bankCode, String currency) throws IOException {
        String url = paystackConfig.getBaseUrl() + "/transferrecipient";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());
            post.setHeader("Content-Type", "application/json");

            String json = String.format("{\"type\": \"nuban\", \"name\": \"%s\", \"account_number\": \"%s\", \"bank_code\": \"%s\", \"currency\": \"%s\"}",
                    name, accountNumber, bankCode, currency);
            post.setEntity(new StringEntity(json));

            HttpResponse response = client.execute(post);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public String initiatePayout(double amount, String recipientCode, String reason/*, RecipientDTO recipientDTO*/) throws IOException {
//        String url = paystackConfig.getBaseUrl() + "/transfer";
//
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpPost post = new HttpPost(url);
//            post.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());
//            post.setHeader("Content-Type", "application/json");
//
//            String json = String.format("{\"source\": \"balance\", \"amount\": %.0f, \"recipient\": \"%s\", \"reason\": \"%s\"}", amount * 100, recipientCode, reason);
//            post.setEntity(new StringEntity(json));
//
//            HttpResponse response = client.execute(post);
//            return EntityUtils.toString(response.getEntity());
//        }
//    }

    public TransferResponse initiatePayout(double amount, String recipientCode, String reason) {
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
                .block(); // Synchronous call, blocking until response is received
    }

    public String finalizePayout(String transferCode, String otp) throws IOException {
        String url = paystackConfig.getBaseUrl() +"/transfer/finalize";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());
            post.setHeader("Content-Type", "application/json");

            String json = String.format(
                    "{\"transfer_code\": \"%s\", \"otp\": \"%s\"}",
                    transferCode, otp);
            post.setEntity(new StringEntity(json));

            HttpResponse response = client.execute(post);
            return EntityUtils.toString(response.getEntity());
        }
    }

//    public String finalizeTransfer(String transferCode, String otp) {
//        String url = apiUrl + "/transfer/finalize";
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + secretKey);
//        headers.set("Content-Type", "application/json");
//
//        String requestBody = String.format(
//                "{\"transfer_code\": \"%s\", \"otp\": \"%s\"}",
//                transferCode, otp);
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<TransferResponse> response = restTemplate.exchange(
//                url, HttpMethod.POST, entity, TransferResponse.class);
//        return response.getBody();
//    }

    public String verifyPayout(String transferIdOrCode) throws IOException {
        String url = paystackConfig.getBaseUrl() + "/transfer/" + transferIdOrCode;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());

            HttpResponse response = client.execute(get);
            return EntityUtils.toString(response.getEntity());
        }
    }

    public String getBanks() throws IOException {
        String url = paystackConfig.getBaseUrl() + "/bank";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + paystackConfig.getSecretKey());

            HttpResponse response = client.execute(get);

            return EntityUtils.toString(response.getEntity());

        }
    }
}