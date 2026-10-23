package com.nhom11.qldt.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
/** Lớp cơ sở bổ sung thời điểm tạo và cập nhật tự động cho các thực thể cần kiểm toán. */
public abstract class AuditableEntity extends IdOnlyEntity {
  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt = Instant.now();

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;
}
