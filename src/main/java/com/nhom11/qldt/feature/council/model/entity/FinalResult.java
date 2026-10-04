package com.nhom11.qldt.feature.council.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.model.enums.FinalResultStatus;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "final_results")
@Getter
@Setter
public class FinalResult extends SoftDeleteEntity {
  @OneToOne(optional = false)
  @JoinColumn(name = "topic_registration_id", nullable = false, unique = true)
  private TopicRegistration topicRegistration;

  @Column(name = "final_score", nullable = false, precision = 4, scale = 2)
  private BigDecimal finalScore;

  @Column(name = "summary_comment", columnDefinition = "TEXT")
  private String summaryComment;

  @Enumerated(EnumType.STRING)
  private FinalResultStatus status;

  @ManyToOne
  @JoinColumn(name = "finalized_by")
  private User finalizedBy;

  private LocalDateTime finalizedAt;
}
