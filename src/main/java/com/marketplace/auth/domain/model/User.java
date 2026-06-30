package com.marketplace.auth.domain.model;

import com.marketplace.auth.domain.event.UserRegisteredEvent;
import com.marketplace.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class User extends AggregateRoot {

    private final UserId id;
    private final Email email;
    private String passwordHash;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private final Instant createdAt;

    private User(UserId id, Email email, String passwordHash, String fullName, UserRole role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.createdAt = Instant.now();
    }

    private User(UserId id, Email email, String passwordHash, String fullName,
                 UserRole role, UserStatus status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static User reconstitute(UserId id, Email email, String passwordHash, String fullName,
                                    UserRole role, UserStatus status, Instant createdAt) {
        return new User(id, email, passwordHash, fullName, role, status, createdAt);
    }

    public static User register(String email, String passwordHash, String fullName) {
        User user = new User(
                new UserId(UUID.randomUUID()),
                new Email(email),
                passwordHash,
                fullName,
                UserRole.BUYER
        );
        user.registerEvent(new UserRegisteredEvent(user.id, user.email, Instant.now()));
        return user;
    }

    public void activate() {
        if (status != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("User is not pending verification");
        }
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
