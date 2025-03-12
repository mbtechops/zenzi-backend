package com.zenzi.dto.response.paystack;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookResponse {
    private String message;
}
