package com.nhom11.qldt.feature.reportsubmission.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.auth.model.entity.User;
import com.nhom11.qldt.feature.reportsubmission.model.enums.ReportSubmissionStatus;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_submissions")
@Getter
@Setter
public class ReportSubmission extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "topic_registration_id", nullable = false)
  private TopicRegistration topicRegistration;

  @ManyToOne(optional = false)
  @JoinColumn(name = "submitted_by", nullable = false)
  private User submittedBy;

  @Enumerated(EnumType.STRING)
  private ReportSubmissionStatus status;

  @ManyToOne
  @JoinColumn(name = "replaced_submission_id")
  private ReportSubmission replacedSubmission;

  private LocalDateTime submittedAt;

  @OneToMany(mappedBy = "reportSubmission")
  private List<ReportFile> files = new ArrayList<>();
}
