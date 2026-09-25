package com.nhom11.qldt.feature.topic.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
public class Topic extends SoftDeleteEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "registration_period_id", nullable = false)
  private RegistrationPeriod registrationPeriod;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;
}
