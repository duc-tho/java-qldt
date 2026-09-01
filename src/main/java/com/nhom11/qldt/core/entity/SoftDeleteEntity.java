package com.nhom11.qldt.core.entity;

import java.time.Instant;
import org.springframework.data.annotation.LastModifiedDate;
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
abstract public class SoftDeleteEntity extends AuditableEntity {
    @LastModifiedDate
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
