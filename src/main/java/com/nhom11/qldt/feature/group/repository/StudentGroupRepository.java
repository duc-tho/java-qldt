package com.nhom11.qldt.feature.group.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.group.model.entity.StudentGroup;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupRepository extends BaseRepository<StudentGroup, Long> {
  long countByRegistrationPeriodId(Long registrationPeriodId);

  boolean existsByRegistrationPeriodIdAndNameIgnoreCase(Long registrationPeriodId, String name);

  @EntityGraph(attributePaths = {"registrationPeriod"})
  Optional<StudentGroup> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  List<StudentGroup> findAllByRegistrationPeriodIdOrderByName(Long registrationPeriodId);

  @Query(
      """
      select distinct studentGroup from StudentGroup studentGroup
      join TopicRegistration registration on registration.studentGroup = studentGroup
      join registration.topic topic
      left join topic.advisers adviser
      left join CouncilMember member on member.council = topic.council
      where studentGroup.registrationPeriod.id = :periodId
        and (adviser.id = :lecturerId or topic.reviewer.id = :lecturerId
          or member.lecturer.id = :lecturerId)
      order by studentGroup.name
      """)
  List<StudentGroup> findAllRelatedToLecturer(
      @Param("periodId") Long periodId, @Param("lecturerId") Long lecturerId);

  @Query(
      """
      select distinct studentGroup from StudentGroup studentGroup
      join TopicRegistration registration on registration.studentGroup = studentGroup
      join registration.topic topic
      join topic.advisers adviser
      where studentGroup.registrationPeriod.id = :periodId
        and adviser.id = :lecturerId
        and registration.status = com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus.PENDING
      order by studentGroup.name
      """)
  List<StudentGroup> findAllPendingForAdviser(
      @Param("periodId") Long periodId, @Param("lecturerId") Long lecturerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select studentGroup from StudentGroup studentGroup where studentGroup.id = :id")
  Optional<StudentGroup> findByIdForUpdate(@Param("id") Long id);
}
