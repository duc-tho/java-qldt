package com.nhom11.qldt.feature.auth.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.nhom11.qldt.core.service.BaseServiceImpl;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.auth.repository.UserRepository;

public class UserService extends BaseServiceImpl<User, Long> {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }
}
