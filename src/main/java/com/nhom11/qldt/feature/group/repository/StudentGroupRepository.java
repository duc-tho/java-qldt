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

/** Truy cập và truy vấn dữ liệu của chức năng nhóm sinh viên và đăng ký đề tài. */
public interface StudentGroupRepository extends BaseRepository<StudentGroup, Long> {
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  long countByRegistrationPeriodId(Long registrationPeriodId);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByRegistrationPeriodIdAndNameIgnoreCase(Long registrationPeriodId, String name);

  @EntityGraph(attributePaths = {"registrationPeriod"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<StudentGroup> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<StudentGroup> findAllPendingForAdviser(
      @Param("periodId") Long periodId, @Param("lecturerId") Long lecturerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select studentGroup from StudentGroup studentGroup where studentGroup.id = :id")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<StudentGroup> findByIdForUpdate(@Param("id") Long id);
}
