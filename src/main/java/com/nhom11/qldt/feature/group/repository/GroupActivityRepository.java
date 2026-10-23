package com.nhom11.qldt.feature.group.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.group.model.entity.GroupActivity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

/** Truy cập và truy vấn dữ liệu của chức năng nhóm sinh viên và đăng ký đề tài. */
public interface GroupActivityRepository extends BaseRepository<GroupActivity, Long> {
  @EntityGraph(attributePaths = {"actor", "subject"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<GroupActivity> findAllByStudentGroupIdOrderByOccurredAtDescIdDesc(Long groupId);
}
