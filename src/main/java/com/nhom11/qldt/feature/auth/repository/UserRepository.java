package com.nhom11.qldt.feature.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.auth.model.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
}
