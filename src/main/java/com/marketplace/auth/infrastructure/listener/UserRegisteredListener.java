package com.marketplace.auth.infrastructure.listener;

import com.marketplace.auth.application.port.VerificationMailer;
import com.marketplace.auth.domain.event.UserRegisteredEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredListener {

    private final VerificationMailer verificationMailer;

    public UserRegisteredListener(VerificationMailer verificationMailer) {
        this.verificationMailer = verificationMailer;
    }

    @Async
    @TransactionalEventListener
    public void handle(UserRegisteredEvent event) {
        verificationMailer.sendVerificationMail(event.email().value());
    }
}
