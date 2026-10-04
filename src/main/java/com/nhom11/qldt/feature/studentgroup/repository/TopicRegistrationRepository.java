package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.studentgroup.model.enums.TopicRegistrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRegistrationRepository extends BaseRepository<TopicRegistration, Long> {
  @EntityGraph(
      attributePaths = {
        "studentGroup",
        "topic",
        "topic.department",
        "topic.advisers",
        "reviewedBy"
      })
  Optional<TopicRegistration> findByStudentGroupId(Long studentGroupId);

  @Query(
      """
      select distinct registration from TopicRegistration registration
      join registration.topic topic
      join topic.advisers adviser
      where adviser.id = :lecturerId and registration.status = :status
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByAdviserIdAndStatus(
      @Param("lecturerId") Long lecturerId, @Param("status") TopicRegistrationStatus status);

  @Query(
      """
      select registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join fetch topic.department
      where studentGroup.registrationPeriod.id = :periodId
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByRegistrationPeriodId(@Param("periodId") Long periodId);

  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join topic.advisers adviser
      where registration.id = :registrationId and adviser.id = :lecturerId
      """)
  Optional<TopicRegistration> findByIdAndAdviserId(
      @Param("registrationId") Long registrationId, @Param("lecturerId") Long lecturerId);

  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join CouncilMember member on member.council = topic.council
      where member.lecturer.id = :lecturerId
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByCouncilMemberId(@Param("lecturerId") Long lecturerId);
}
