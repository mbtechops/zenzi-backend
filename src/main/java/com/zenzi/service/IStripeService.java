package com.zenzi.service;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenzi.dto.BalanceDTO;
import com.zenzi.dto.ProductRequest;
import com.zenzi.dto.StripeResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IStripeService {

    StripeResponse createCheckoutSession(ProductRequest productRequest);
    BalanceDTO getBalance() throws StripeException;
    String handleStripeWebhook(HttpServletRequest request) throws IOException, EventDataObjectDeserializationException;
}
