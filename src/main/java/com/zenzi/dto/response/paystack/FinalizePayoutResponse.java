package com.zenzi.dto.response.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalizePayoutResponse {

    private boolean status;
    private String message;
    private String type;
    private String code;
    private ResponseMeta meta;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseMeta {
        private String nextStep;
    }

}

