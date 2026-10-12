package com.nhom11.qldt.feature.topic.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.council.model.entity.Council;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
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

  @ManyToOne
  @JoinColumn(name = "council_id")
  private Council council;

  @ManyToOne
  @JoinColumn(name = "reviewer_id")
  private User reviewer;

  @ManyToOne
  @JoinColumn(name = "proposed_by")
  private User proposedBy;

  @ManyToOne(optional = false)
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;

  @ManyToMany
  @JoinTable(
      name = "topic_advisors",
      joinColumns = @JoinColumn(name = "topic_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<User> advisers = new HashSet<>();

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String rejectionReason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TopicStatus status;
}
