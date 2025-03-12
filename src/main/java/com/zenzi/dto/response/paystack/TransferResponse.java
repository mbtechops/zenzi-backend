package com.zenzi.dto.response.paystack;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponse {
    private boolean status;
    private String message;
    private TransferData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferData {
        private String transfer_code;
        private String status;
    }
}