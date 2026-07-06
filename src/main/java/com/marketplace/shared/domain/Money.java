package com.marketplace.shared.domain;

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

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies: " + currency + " + " + other.currency);
        }
        return new Money(amount.add(other.amount), currency);
    }
}
