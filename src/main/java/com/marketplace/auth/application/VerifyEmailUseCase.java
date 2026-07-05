package com.marketplace.auth.application;

import com.marketplace.auth.application.command.VerifyEmailCommand;
import com.marketplace.auth.application.port.OtpStore;
import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final OtpStore otpStore;

    public VerifyEmailUseCase(UserRepository userRepository, OtpStore otpStore) {
        this.userRepository = userRepository;
        this.otpStore = otpStore;
    }

    @Transactional
    public void execute(VerifyEmailCommand command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "No user with email: " + command.email()));

        if (user.isActive()) {
            throw new BusinessException("ALREADY_VERIFIED", "Account is already verified");
        }

        if (!otpStore.verify(command.email(), command.otp())) {
            throw new BusinessException("INVALID_OTP", "OTP is invalid or expired");
        }

        user.activate();
        userRepository.save(user);
    }
}
