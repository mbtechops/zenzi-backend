package com.zenzi.dto.response.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {

    private String status;
    private String message;
    private List<BankDTO> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankDTO {

        private int id;
        private String name;
        private String code;
        private String country;
        private String currency;
        private boolean active;
    }
}