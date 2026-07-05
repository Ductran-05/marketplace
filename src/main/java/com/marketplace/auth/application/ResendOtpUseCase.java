package com.marketplace.auth.application;

import com.marketplace.auth.application.port.OtpStore;
import com.marketplace.auth.application.port.VerificationMailer;
import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class ResendOtpUseCase {

    private final UserRepository userRepository;
    private final VerificationMailer verificationMailer;
    private final OtpStore otpStore;

    public ResendOtpUseCase(UserRepository userRepository,
                            VerificationMailer verificationMailer,
                            OtpStore otpStore) {
        this.userRepository = userRepository;
        this.verificationMailer = verificationMailer;
        this.otpStore = otpStore;
    }

    public void execute(String email) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "No user with email: " + email));

        if (user.isActive()) {
            throw new BusinessException("ALREADY_VERIFIED", "Account is already verified");
        }

        if (!otpStore.tryStartResendCooldown(email)) {
            throw new BusinessException("RESEND_TOO_SOON", "Please wait 60 seconds before requesting a new code");
        }

        verificationMailer.sendVerificationMail(email);
    }
}
