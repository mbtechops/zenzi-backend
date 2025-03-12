package com.zenzi.dto.response.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponse {

    private boolean status;
    private String message;
    private List<BalanceData> data;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BalanceData {
        private String currency;
        private long balance;
    }
}