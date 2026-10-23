package com.nhom11.qldt.feature.group.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
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
@Table(name = "topic_registrations")
@Getter
@Setter
/** Biểu diễn yêu cầu của nhóm đăng ký thực hiện một đề tài. */
public class TopicRegistration extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_group_id", nullable = false)
  private StudentGroup studentGroup;

  @ManyToOne(optional = false)
  @JoinColumn(name = "topic_id", nullable = false)
  private Topic topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TopicRegistrationStatus status;

  @Column(columnDefinition = "TEXT")
  private String rejectionReason;

  @ManyToOne
  @JoinColumn(name = "reviewed_by")
  private User reviewedBy;

  private LocalDateTime reviewedAt;
}
