package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.StudentGroup;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupRepository extends BaseRepository<StudentGroup, Long> {
  @EntityGraph(attributePaths = {"registrationPeriod"})
  Optional<StudentGroup> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  List<StudentGroup> findAllByRegistrationPeriodIdOrderByName(Long registrationPeriodId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select studentGroup from StudentGroup studentGroup where studentGroup.id = :id")
  Optional<StudentGroup> findByIdForUpdate(@Param("id") Long id);
}
