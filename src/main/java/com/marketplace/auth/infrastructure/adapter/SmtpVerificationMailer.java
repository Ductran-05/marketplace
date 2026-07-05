package com.marketplace.auth.infrastructure.adapter;

import com.marketplace.auth.application.port.OtpStore;
import com.marketplace.auth.application.port.VerificationMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpVerificationMailer implements VerificationMailer {

    private static final Logger log = LoggerFactory.getLogger(SmtpVerificationMailer.class);

    private final OtpStore otpStore;
    private final JavaMailSender mailSender;

    public SmtpVerificationMailer(OtpStore otpStore, JavaMailSender mailSender) {
        this.otpStore = otpStore;
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationMail(String email) {
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
