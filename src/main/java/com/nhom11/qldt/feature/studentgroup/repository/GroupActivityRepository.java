package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.GroupActivity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface GroupActivityRepository extends BaseRepository<GroupActivity, Long> {
  @EntityGraph(attributePaths = {"actor", "subject"})
  List<GroupActivity> findAllByStudentGroupIdOrderByOccurredAtDescIdDesc(Long groupId);
}
