package com.marketplace.auth.application;

import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.model.UserId;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Cửa công khai của auth module cho module khác tra thông tin liên hệ user. */
@Service
public class GetUserContactUseCase {

    private final UserRepository userRepository;

    public GetUserContactUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserContact execute(UUID userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        return new UserContact(user.getEmail().value(), user.getFullName());
    }

    public record UserContact(String email, String fullName) {}
}
