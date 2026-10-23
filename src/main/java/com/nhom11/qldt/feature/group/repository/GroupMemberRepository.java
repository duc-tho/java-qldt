package com.nhom11.qldt.feature.group.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.group.model.entity.GroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng nhóm sinh viên và đăng ký đề tài. */
public interface GroupMemberRepository extends BaseRepository<GroupMember, Long> {
  @EntityGraph(attributePaths = {"studentGroup", "student", "student.roles"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<GroupMember> findByStudentGroupIdAndStudentId(Long studentGroupId, Long studentId);

  @EntityGraph(attributePaths = {"student"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<GroupMember> findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(Long studentGroupId);

  @EntityGraph(attributePaths = "student")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<GroupMember> findAllByStudentGroupIdIn(List<Long> studentGroupIds);

  @Query(
      """
      select member from GroupMember member
      where member.student.id = :studentId
        and member.studentGroup.registrationPeriod.id = :periodId
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<GroupMember> findByStudentIdAndPeriodId(
      @Param("studentId") Long studentId, @Param("periodId") Long periodId);

  @EntityGraph(attributePaths = {"studentGroup", "studentGroup.registrationPeriod"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<GroupMember> findAllByStudentIdOrderByCreatedAtDesc(Long studentId);
}
