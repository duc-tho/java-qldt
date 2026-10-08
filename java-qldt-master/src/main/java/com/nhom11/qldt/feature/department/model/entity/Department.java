package com.nhom11.qldt.feature.department.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department extends SoftDeleteEntity {

  @Column(nullable = false, unique = true, length = 30)
  private String code;

  @Column(nullable = false, unique = true, length = 255)
  private String name;
}
