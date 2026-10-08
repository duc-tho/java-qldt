package com.nhom11.qldt.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeleteEntity extends AuditableEntity {
  @Column(name = "deleted_at")
  private Instant deletedAt;
}
