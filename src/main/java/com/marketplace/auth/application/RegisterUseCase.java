package com.marketplace.auth.application;

import com.marketplace.auth.application.command.RegisterCommand;
import com.marketplace.auth.domain.model.Email;
import com.marketplace.auth.domain.model.User;
import com.marketplace.auth.domain.model.UserId;
import com.marketplace.auth.domain.repository.UserRepository;
import com.marketplace.shared.domain.DomainEvent;
import com.marketplace.shared.exception.BusinessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterUseCase(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserId execute(RegisterCommand command) {
        Email email = new Email(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_TAKEN", "Email already registered: " + command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.register(command.email(), passwordHash, command.fullName());
        userRepository.save(user);

        List<DomainEvent> events = user.pullEvents();
        events.forEach(eventPublisher::publishEvent);

        return user.getId();
    }
}
