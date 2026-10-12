package com.nhom11.qldt.feature.council.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "councils")
@Getter
@Setter
public class Council extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "registration_period_id", nullable = false)
  private RegistrationPeriod registrationPeriod;

  @ManyToOne(optional = false)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  private String name;
}
