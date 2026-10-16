package com.nhom11.qldt.feature.report.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.report.model.entity.ReportSubmission;
import com.nhom11.qldt.feature.report.model.enums.ReportSubmissionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ReportSubmissionRepository extends BaseRepository<ReportSubmission, Long> {
  Optional<ReportSubmission> findFirstByTopicRegistrationIdAndStatusNotOrderBySubmittedAtDesc(
      Long registrationId, ReportSubmissionStatus status);

  @EntityGraph(attributePaths = "files")
  List<ReportSubmission> findAllByTopicRegistrationIdOrderBySubmittedAtDesc(Long registrationId);
}
