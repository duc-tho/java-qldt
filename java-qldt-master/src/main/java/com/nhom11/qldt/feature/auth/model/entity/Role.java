package com.nhom11.qldt.feature.auth.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Role extends SoftDeleteEntity {
  @Column(nullable = false, unique = true, length = 50)
  String name;

  @ManyToMany(mappedBy = "roles")
  private Set<User> users = new HashSet<>();
}
