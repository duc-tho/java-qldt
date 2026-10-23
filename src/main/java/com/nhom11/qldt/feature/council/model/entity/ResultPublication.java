package com.nhom11.qldt.feature.council.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.model.enums.ResultPublicationAction;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "result_publications")
@Getter
@Setter
/** Ghi nhận lịch sử công bố hoặc thu hồi kết quả của một đợt. */
public class ResultPublication extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "registration_period_id", nullable = false)
  private RegistrationPeriod registrationPeriod;

  @Enumerated(EnumType.STRING)
  private ResultPublicationAction action;

  @Column(length = 500)
  private String reason;

  @ManyToOne(optional = false)
  @JoinColumn(name = "performed_by", nullable = false)
  private User performedBy;

  private LocalDateTime performedAt;
}
