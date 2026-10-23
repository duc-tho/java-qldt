package com.nhom11.qldt.feature.group.model.entity;

import com.nhom11.qldt.core.entity.AuditableEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.group.model.enums.GroupActivityType;
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
@Table(name = "group_activities")
@Getter
@Setter
/** Ghi lại một sự kiện nghiệp vụ trong lịch sử hoạt động của nhóm. */
public class GroupActivity extends AuditableEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_group_id", nullable = false)
  private StudentGroup studentGroup;

  @ManyToOne(optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @ManyToOne
  @JoinColumn(name = "subject_id")
  private User subject;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private GroupActivityType type;

  @Column(columnDefinition = "TEXT")
  private String detail;

  @Column(nullable = false)
  private LocalDateTime occurredAt;
}
