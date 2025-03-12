package com.zenzi.service;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.zenzi.dto.response.BalanceDTO;
import com.zenzi.dto.request.ProductRequest;
import com.zenzi.dto.response.StripeResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IStripeService {

    StripeResponse createCheckoutSession(ProductRequest productRequest);
    BalanceDTO getBalance() throws StripeException;
    String handleStripeWebhook(HttpServletRequest request) throws IOException, EventDataObjectDeserializationException;
}
