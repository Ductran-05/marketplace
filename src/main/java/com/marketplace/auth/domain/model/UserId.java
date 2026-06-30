package com.marketplace.auth.domain.model;

import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null) throw new IllegalArgumentException("UserId cannot be null");
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
