package com.nhom11.qldt.feature.auth.repository;

import java.util.Optional;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.auth.model.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
