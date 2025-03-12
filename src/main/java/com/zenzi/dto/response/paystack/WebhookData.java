package com.zenzi.dto.response.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//public  class WebhookData {
//
//    private long id;
//
//    private int amount;
//
//    private String currency;
//
//    private String recipient;
//
//    private String transfer_code;
//
//    private String status;
//
//    private String created_at;
//
//    private String updated_at;
//}












@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookData {
    private long id;
    private int amount;
    private String currency;
    private RecipientData recipient; // Changed from String to RecipientData
    private String transfer_code;
    private String status;
    private String created_at;
    private String updated_at;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipientData {
        private boolean active;
        private String createdAt;
        private String currency;
        private int id;
        private String name;
        private String recipient_code;
        private String type;
        private BankDetails details;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BankDetails {
            private String account_number;
            private String account_name;
            private String bank_code;
            private String bank_name;
        }
    }
}


