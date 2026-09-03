package com.nhom11.qldt.feature.auth.repository;

import org.springframework.stereotype.Repository;
import com.nhom11.qldt.core.repository.BaseRepositoryImpl;
import com.nhom11.qldt.feature.auth.model.entity.User;
import jakarta.persistence.EntityManager;

@Repository
public class UserRepository extends BaseRepositoryImpl<User, Long> {
    public UserRepository(EntityManager entityManager) {
        super(User.class, entityManager);
    }
}
