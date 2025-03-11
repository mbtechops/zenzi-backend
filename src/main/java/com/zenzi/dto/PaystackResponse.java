package com.zenzi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackResponse {
    private boolean status;
    private String message;
    private Data data;

    public static class Data {
        private String authorization_url;
        private String reference;

        // Getters and Setters
    }

    // Getters and Setters
}
