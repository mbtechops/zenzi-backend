package com.zenzi.dto.response.paystack;

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
        private boolean active;
        private String createdAt;
        private String currency;
        private String description;
        private String domain;
        private String email;
        private int id;
        private int integration;
        private Object metadata;
        private String name;
        private String recipient_code;
        private String type;
        private String updatedAt;
        private boolean is_deleted;
        private boolean isDeleted;
        private BankDetails details;


        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BankDetails {
            private String authorization_code;
            private String account_number;
            private String account_name;
            private String bank_code;
            private String bank_name;
        }
    }
}



