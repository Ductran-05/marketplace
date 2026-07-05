package com.marketplace.auth.application.port;

public interface VerificationMailer {

    /** Sinh OTP mới và gửi mail xác thực tới email này. */
    void sendVerificationMail(String email);
}
