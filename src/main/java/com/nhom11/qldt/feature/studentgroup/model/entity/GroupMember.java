package com.nhom11.qldt.feature.studentgroup.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "group_members")
@Getter
@Setter
public class GroupMember extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "student_group_id", nullable = false)
  private StudentGroup studentGroup;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  private boolean isLeader;
}
