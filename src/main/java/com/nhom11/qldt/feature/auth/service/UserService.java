package com.nhom11.qldt.feature.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;

@Service
public class UserService extends BaseCrudService<User, Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    protected void beforeCreate(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Override
    protected void beforeUpdate(User updates) {
        userRepository.findById(updates.getId()).ifPresent(existingUser -> {
            if (updates.getPassword() == null || updates.getPassword().isBlank()) {
                updates.setPassword(existingUser.getPassword());
            } else {
                updates.setPassword(passwordEncoder.encode(updates.getPassword()));
            }

            updates.setRoles(existingUser.getRoles());
        });
    }
}
