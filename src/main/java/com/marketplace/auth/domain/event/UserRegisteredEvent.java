package com.marketplace.auth.domain.event;

import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.UserId;
import com.marketplace.shared.domain.DomainEvent;

import java.time.Instant;

public record UserRegisteredEvent(
        UserId userId,
        Email email,
        Instant occurredAt
) implements DomainEvent {}
