package com.zenzi.dto.response.paystack;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyPaymentResponse {
    private boolean status;
    private String message;
    private TransactionData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionData {
        private long id;
        private String domain;
        private String status;
        private String reference;
        private String receipt_number;
        private int amount;
        private String message;
        private String gateway_response;
        private String paid_at;
        private String created_at;
        private String channel;
        private String currency;
        private String ip_address;
        private String metadata;
        private Object log;
        private Integer fees;
        private Object fees_split;
        private Customer customer;
        private Object plan;
        private String order_id;
        private String paidAt;
        private String createdAt;
        private int requested_amount;
        private Object pos_transaction_data;
        private Object source;
        private Object fees_breakdown;
        private Object connect;
        private String transaction_date;


        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {
            private long id;
            private String first_name;
            private String last_name;
            private String email;
            private String customer_code;
            private String phone;
            private Object metadata;
            private String risk_action;
            private String international_format_phone;
        }
    }
}
//
//{
//        "status":true,
//        "message":"Verification successful",
//        "data":{"" +
//        "id":4770185247,
//        "domain":"test",
//        "status":"abandoned",
//        "reference":"fjche08zpf",
//        "receipt_number":null,
//        "amount":100000,
//        "message":null,
//        "gateway_response":"The transaction was not completed",
//        "paid_at":null,
//        "created_at":"2025-03-11T17:48:10.000Z",
//        "channel":"card",
//        "currency":"NGN",
//        "ip_address":"98.97.78.151, 172.68.102.169, 172.31.62.52",
//        "metadata":"",
//        "log":null,
//        "fees":null,
//        "fees_split":null,
//        "authorization":{},
//        "customer":{
//        "id":250225234,
//        "first_name":null,
//        "last_name":null,
//        "email":"dean@mariblock.com",
//        "customer_code":"CUS_i76x22wi3nyrro1",
//        "phone":null,
//        "metadata":null,
//        "risk_action":"default",
//        "international_format_phone":null
//        },
//        "plan":null,
//        "split":{},
//        "order_id":null,
//        "paidAt":null,
//        "createdAt":"2025-03-11T17:48:10.000Z",
//        "requested_amount":100000,
//        "pos_transaction_data":null,
//        "source":null,
//        "fees_breakdown":null,
//        "connect":null,
//        "transaction_date":"2025-03-11T17:48:10.000Z",
//        "plan_object":{},
//        "subaccount":{}
//        }
//        }