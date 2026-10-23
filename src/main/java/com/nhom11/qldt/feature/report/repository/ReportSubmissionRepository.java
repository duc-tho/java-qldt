package com.nhom11.qldt.feature.report.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.report.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.report.model.enums.ReportSubmissionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

/** Truy cập và truy vấn dữ liệu của chức năng nộp báo cáo. */
public interface ReportSubmissionRepository extends BaseRepository<ReportSubmission, Long> {
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<ReportSubmission> findFirstByTopicRegistrationIdAndStatusNotOrderBySubmittedAtDesc(
      Long registrationId, ReportSubmissionStatus status);

  @EntityGraph(attributePaths = "files")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<ReportSubmission> findAllByTopicRegistrationIdOrderBySubmittedAtDesc(Long registrationId);
}
