package com.marketplace.auth.infrastructure.listener;

import com.marketplace.auth.application.port.OtpStore;
import com.marketplace.auth.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    private final OtpStore otpStore;
    private final JavaMailSender mailSender;

    public UserRegisteredListener(OtpStore otpStore, JavaMailSender mailSender) {
        this.otpStore = otpStore;
        this.mailSender = mailSender;
    }

    @Async
    @TransactionalEventListener
    public void handle(UserRegisteredEvent event) {
        String email = event.email().value();
        String otp = otpStore.generate(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@marketplace.local");
        message.setTo(email);
        message.setSubject("Marketplace - Verify your email");
        message.setText("""
                Welcome to Marketplace!

                Your verification code is: %s

                This code expires in 15 minutes.
                """.formatted(otp));

        mailSender.send(message);
        log.info("Verification email sent to {}", email);
    }
}
