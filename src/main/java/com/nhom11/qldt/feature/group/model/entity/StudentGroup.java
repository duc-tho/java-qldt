package com.nhom11.qldt.feature.group.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student_groups")
@Getter
@Setter
/** Biểu diễn nhóm sinh viên thực hiện đề tài trong một đợt. */
public class StudentGroup extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "registration_period_id", nullable = false)
  private RegistrationPeriod registrationPeriod;

  @Column(nullable = false, length = 150)
  private String name;
}
