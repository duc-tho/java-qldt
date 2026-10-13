package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface StudentGroupRepository extends BaseRepository<StudentGroup, Long> {
  @EntityGraph(attributePaths = {"registrationPeriod"})
  Optional<StudentGroup> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  List<StudentGroup> findAllByRegistrationPeriodIdOrderByName(Long registrationPeriodId);
}
