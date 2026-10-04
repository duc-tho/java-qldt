package com.nhom11.qldt.feature.council.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.model.enums.EvaluationStatus;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
public class Evaluation extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "topic_registration_id", nullable = false)
  private TopicRegistration topicRegistration;

  @ManyToOne(optional = false)
  @JoinColumn(name = "evaluator_id", nullable = false)
  private User evaluator;

  @Column(precision = 4, scale = 2)
  private BigDecimal score;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Enumerated(EnumType.STRING)
  private EvaluationStatus status;

  private LocalDateTime submittedAt;
}
