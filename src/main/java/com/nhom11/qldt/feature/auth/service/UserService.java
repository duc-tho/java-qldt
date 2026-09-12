package com.nhom11.qldt.feature.auth.service;

import org.springframework.stereotype.Service;

import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;

@Service
public class UserService extends BaseCrudService<User, Long> {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    @Override
    protected void beforeUpdate(User updates) {
        userRepository.findById(updates.getId()).ifPresent(existingUser -> {
            if (updates.getPassword() == null || updates.getPassword().isBlank()) {
                updates.setPassword(existingUser.getPassword());
            }

            updates.setRoles(existingUser.getRoles());
        });
    }
}
