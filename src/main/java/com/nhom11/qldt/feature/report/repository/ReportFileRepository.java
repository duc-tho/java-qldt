package com.nhom11.qldt.feature.report.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.report.model.entity.ReportFile;
import java.util.List;

public interface ReportFileRepository extends BaseRepository<ReportFile, Long> {
  List<ReportFile> findAllByReportSubmissionId(Long submissionId);
}
