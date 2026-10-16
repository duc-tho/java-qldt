package com.nhom11.qldt.feature.group.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRegistrationRepository extends BaseRepository<TopicRegistration, Long> {
  @Query(
      "select registration.studentGroup.registrationPeriod.id, count(registration) from TopicRegistration registration where registration.studentGroup.registrationPeriod.id in :periodIds and registration.status = :status group by registration.studentGroup.registrationPeriod.id")
  List<Object[]> countByPeriodIdsAndStatus(
      @Param("periodIds") List<Long> periodIds,
      @Param("status") TopicRegistrationStatus status);

  @EntityGraph(attributePaths = {"topic", "topic.department"})
  List<TopicRegistration> findAllByStudentGroupIdIn(List<Long> groupIds);

  @EntityGraph(attributePaths = {"studentGroup", "studentGroup.registrationPeriod"})
  List<TopicRegistration> findAllByTopicId(Long topicId);

  @Query(
      """
      select registration.studentGroup.id from TopicRegistration registration
      join registration.topic.advisers adviser
      where registration.id = :registrationId and adviser.id = :lecturerId
      """)
  Optional<Long> findGroupIdByIdAndAdviserId(
      @Param("registrationId") Long registrationId, @Param("lecturerId") Long lecturerId);

  @EntityGraph(
      attributePaths = {
        "studentGroup",
        "topic",
        "topic.department",
        "topic.advisers",
        "reviewedBy"
      })
  Optional<TopicRegistration> findByStudentGroupId(Long studentGroupId);

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
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

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch studentGroup.registrationPeriod
      join fetch registration.topic topic
      join fetch topic.department
      join topic.advisers adviser
      where adviser.id = :lecturerId and registration.status <> :excludedStatus
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByAdviserIdExcludingStatus(
      @Param("lecturerId") Long lecturerId,
      @Param("excludedStatus") TopicRegistrationStatus excludedStatus);

  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch studentGroup.registrationPeriod
      join fetch registration.topic topic
      join fetch topic.department
      join topic.advisers adviser
      where adviser.id = :lecturerId
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByAdviserId(@Param("lecturerId") Long lecturerId);

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      where topic.reviewer.id = :lecturerId and registration.status = :status
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByReviewerIdAndStatus(
      @Param("lecturerId") Long lecturerId, @Param("status") TopicRegistrationStatus status);

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
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

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
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

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join CouncilMember member on member.council = topic.council
      where member.lecturer.id = :lecturerId
        and registration.status = :status
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByCouncilMemberId(
      @Param("lecturerId") Long lecturerId, @Param("status") TopicRegistrationStatus status);
}
