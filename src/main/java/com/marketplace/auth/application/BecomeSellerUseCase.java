package com.marketplace.auth.application;

import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.model.UserId;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BecomeSellerUseCase {

    private final UserRepository userRepository;

    public BecomeSellerUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(UUID userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        try {
            user.becomeSeller();
        } catch (IllegalStateException e) {
            throw new BusinessException("CANNOT_BECOME_SELLER", e.getMessage());
        }
        userRepository.save(user);
    }
}
