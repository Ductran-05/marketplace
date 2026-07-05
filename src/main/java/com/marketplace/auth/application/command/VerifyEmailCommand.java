package com.marketplace.auth.application.command;

public record VerifyEmailCommand(String email, String otp) {}
