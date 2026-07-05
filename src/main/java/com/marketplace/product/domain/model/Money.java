package com.marketplace.product.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be zero or positive");
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter code");
        }
        currency = currency.toUpperCase();
    }

    public static Money vnd(BigDecimal amount) {
        return new Money(amount, "VND");
    }
}
