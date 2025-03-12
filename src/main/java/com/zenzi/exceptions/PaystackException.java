package com.zenzi.exceptions;


import lombok.Getter;

@Getter
public class PaystackException extends RuntimeException {
    private final String errorMessage;
    private final int statusCode;
    private final String paystackError;

    // Constructor for basic message
    public PaystackException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.statusCode = -1; // -1 indicates no HTTP status
        this.paystackError = null;
    }

    // Constructor with HTTP status
    public PaystackException(String errorMessage, int statusCode) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.paystackError = null;
    }

    // Constructor with Paystack error details
    public PaystackException(String errorMessage, int statusCode, String paystackError) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.paystackError = paystackError;
    }

    // Getters
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public int getStatusCode() {
//        return statusCode;
//    }
//
//    public String getPaystackError() {
//        return paystackError;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PaystackApiException: ");
        sb.append(errorMessage);

        if (statusCode != -1) {
            sb.append(" (HTTP Status: ").append(statusCode).append(")");
        }
        if (paystackError != null) {
            sb.append(" [Paystack Error: ").append(paystackError).append("]");
        }
        return sb.toString();
    }
}