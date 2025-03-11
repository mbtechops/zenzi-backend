package com.zenzi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRecipientResponse {
    private boolean status;
    private String message;
    private RecipientData data;



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipientData {
        private String recipient_code;
    }
}