package com.zenzi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {
    private boolean status;
    private String message;
    private TransactionData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionData {
        private String reference;
        private String authorization_url;
    }
}


