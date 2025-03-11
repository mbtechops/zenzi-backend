package com.zenzi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientDTO {

    private String currency;
    private String recipient_code;
    private String name;
    private Details details;



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Details {

        private String account_number;
        private String account_name;
        private String bank_code;
        private String bank_name;
        private String authorization_code;
    }
}
