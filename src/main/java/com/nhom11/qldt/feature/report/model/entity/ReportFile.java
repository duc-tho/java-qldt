package com.nhom11.qldt.feature.report.model.entity;

import com.nhom11.qldt.core.entity.SoftDeleteEntity;
import com.nhom11.qldt.feature.report.model.enums.ReportFileType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_files")
@Getter
@Setter
/** Biểu diễn tệp vật lý thuộc một lần nộp báo cáo. */
public class ReportFile extends SoftDeleteEntity {
  @ManyToOne(optional = false)
  @JoinColumn(name = "report_submission_id", nullable = false)
  private ReportSubmission reportSubmission;

  private String originalName;
  private String storagePath;

  @Enumerated(EnumType.STRING)
  private ReportFileType fileType;

  private long bytes;
}
