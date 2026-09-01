package com.nhom11.qldt.core.entity;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
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
abstract public class AuditableEntity extends IdOnlyEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
