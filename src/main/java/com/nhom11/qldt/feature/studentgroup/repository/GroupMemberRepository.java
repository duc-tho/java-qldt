package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.GroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMemberRepository extends BaseRepository<GroupMember, Long> {
  @EntityGraph(attributePaths = {"studentGroup", "student", "student.roles"})
  Optional<GroupMember> findByStudentGroupIdAndStudentId(Long studentGroupId, Long studentId);

  @EntityGraph(attributePaths = {"student"})
  List<GroupMember> findAllByStudentGroupIdOrderByIsLeaderDescIdAsc(Long studentGroupId);

  @EntityGraph(attributePaths = "student")
  List<GroupMember> findAllByStudentGroupIdIn(List<Long> studentGroupIds);

  @Query(
      """
      select member from GroupMember member
      where member.student.id = :studentId
        and member.studentGroup.registrationPeriod.id = :periodId
      """)
  Optional<GroupMember> findByStudentIdAndPeriodId(
      @Param("studentId") Long studentId, @Param("periodId") Long periodId);

  @EntityGraph(attributePaths = {"studentGroup", "studentGroup.registrationPeriod"})
  List<GroupMember> findAllByStudentIdOrderByCreatedAtDesc(Long studentId);
}
