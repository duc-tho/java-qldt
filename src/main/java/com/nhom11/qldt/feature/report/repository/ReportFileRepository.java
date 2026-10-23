package com.nhom11.qldt.feature.report.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.report.model.entity.ReportFile;
import java.util.List;

/** Truy cập và truy vấn dữ liệu của chức năng nộp báo cáo. */
public interface ReportFileRepository extends BaseRepository<ReportFile, Long> {
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<ReportFile> findAllByReportSubmissionId(Long submissionId);
}
