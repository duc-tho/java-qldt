package com.nhom11.qldt.feature.auth.repository;

import org.springframework.stereotype.Repository;
import com.nhom11.qldt.core.repository.BaseRepositoryImpl;
import com.nhom11.qldt.feature.auth.model.entity.Permission;

import jakarta.persistence.EntityManager;

@Repository
public class PermissionRepository extends BaseRepositoryImpl<Permission, Long> {
    public PermissionRepository(EntityManager entityManager) {
        super(Permission.class, entityManager);
    }
}
