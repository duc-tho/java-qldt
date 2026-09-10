package com.nhom11.qldt.core.entity;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
abstract public class SoftDeleteEntity extends AuditableEntity {
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
