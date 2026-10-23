package com.nhom11.qldt.feature.group.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.group.model.entity.TopicRegistration;
import com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng nhóm sinh viên và đăng ký đề tài. */
public interface TopicRegistrationRepository extends BaseRepository<TopicRegistration, Long> {
  @Query(
      """
      select count(distinct registration) from TopicRegistration registration
      join registration.topic.advisers adviser
      where adviser.id = :lecturerId
        and registration.status = com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus.PENDING
      """)
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  long countPendingByAdviserId(@Param("lecturerId") Long lecturerId);

  @Query(
      """
      select count(distinct registration) from TopicRegistration registration
      left join registration.topic.advisers adviser
      where registration.status = com.nhom11.qldt.feature.group.model.enums.TopicRegistrationStatus.APPROVED
        and (
          (adviser.id = :lecturerId and registration.topic.registrationPeriod.periodType in (
            com.nhom11.qldt.feature.period.model.enums.PeriodType.MON_HOC,
            com.nhom11.qldt.feature.period.model.enums.PeriodType.NCKH
          ))
          or registration.topic.reviewer.id = :lecturerId
          or exists (select member.id from CouncilMember member
            where member.council = registration.topic.council
              and member.lecturer.id = :lecturerId)
        )
        and not exists (select evaluation.id from Evaluation evaluation
          where evaluation.topicRegistration = registration
            and evaluation.evaluator.id = :lecturerId
            and evaluation.status = com.nhom11.qldt.feature.council.model.enums.EvaluationStatus.SUBMITTED)
      """)
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  long countPendingEvaluations(@Param("lecturerId") Long lecturerId);

  @Query(
      "select registration.studentGroup.registrationPeriod.id, count(registration) from TopicRegistration registration where registration.studentGroup.registrationPeriod.id in :periodIds and registration.status = :status group by registration.studentGroup.registrationPeriod.id")
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  List<Object[]> countByPeriodIdsAndStatus(
      @Param("periodIds") List<Long> periodIds,
      @Param("status") TopicRegistrationStatus status);

  @EntityGraph(attributePaths = {"topic", "topic.department"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<TopicRegistration> findAllByStudentGroupIdIn(List<Long> groupIds);

  @EntityGraph(attributePaths = {"studentGroup", "studentGroup.registrationPeriod"})
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<TopicRegistration> findAllByTopicId(Long topicId);

  @Query(
      """
      select registration.studentGroup.id from TopicRegistration registration
      join registration.topic.advisers adviser
      where registration.id = :registrationId and adviser.id = :lecturerId
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<TopicRegistration> findAllByRegistrationPeriodId(@Param("periodId") Long periodId);

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
  @Query(
      """
      select registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      where studentGroup.registrationPeriod.id = :periodId
        and registration.status = :status
      order by registration.updatedAt desc
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<TopicRegistration> findAllByRegistrationPeriodIdAndStatus(
      @Param("periodId") Long periodId, @Param("status") TopicRegistrationStatus status);

  @EntityGraph(attributePaths = {"studentGroup.registrationPeriod", "topic.registrationPeriod"})
  @Query(
      """
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join topic.advisers adviser
      where registration.id = :registrationId and adviser.id = :lecturerId
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
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
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<TopicRegistration> findAllByCouncilMemberId(
      @Param("lecturerId") Long lecturerId, @Param("status") TopicRegistrationStatus status);
}
