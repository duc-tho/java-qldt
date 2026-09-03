package com.nhom11.qldt.feature.auth.repository;

import org.springframework.stereotype.Repository;
import com.nhom11.qldt.core.repository.BaseRepositoryImpl;
import com.nhom11.qldt.feature.auth.model.entity.Role;
import jakarta.persistence.EntityManager;

@Repository
public class RoleRepository extends BaseRepositoryImpl<Role, Long> {
    public RoleRepository(EntityManager entityManager) {
        super(Role.class, entityManager);
    }
}
