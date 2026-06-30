package com.marketplace.auth.application.command;

public record RegisterCommand(String email, String password, String fullName) {}
