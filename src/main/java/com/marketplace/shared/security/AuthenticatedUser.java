package com.marketplace.shared.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, String role) {}
