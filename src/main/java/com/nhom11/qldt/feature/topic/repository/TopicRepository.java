package com.nhom11.qldt.feature.topic.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary;
import com.nhom11.qldt.feature.topic.model.enums.TopicStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRepository extends BaseRepository<Topic, Long> {
  long countByRegistrationPeriodId(Long registrationPeriodId);

  long countByProposedById(Long proposedById);

  @Query(
      """
      select count(topic) from Topic topic
      where topic.status = com.nhom11.qldt.feature.topic.model.enums.TopicStatus.PROPOSED
        and topic.registrationPeriod.lecturerRegistrationStart <= :now
        and topic.registrationPeriod.lecturerRegistrationEnd >= :now
      """)
  long countPendingApprovals(@Param("now") java.time.LocalDateTime now);

  @Query("select topic.council.id, count(topic) from Topic topic where topic.registrationPeriod.id = :periodId and topic.council is not null group by topic.council.id")
  List<Object[]> countByCouncilInPeriod(@Param("periodId") Long periodId);

  @EntityGraph(attributePaths = "department")
  @Query(
      """
      select topic from Topic topic
      where topic.registrationPeriod.id = :registrationPeriodId
        and (:search is null or lower(topic.title) like lower(concat('%', :search, '%')))
        and (:titleFilter is null or lower(topic.title) like lower(concat('%', :titleFilter, '%')))
        and (:departmentId is null or topic.department.id = :departmentId)
        and (:status is null or topic.status = :status)
        and (:lecturerId is null or topic.status = com.nhom11.qldt.feature.topic.model.enums.TopicStatus.APPROVED
          or topic.proposedBy.id = :lecturerId)
        and (:proposedById is null or topic.proposedBy.id = :proposedById)
        and (:adviserSearch is null or exists (
          select adviser.id from Topic searched join searched.advisers adviser
          where searched.id = topic.id and (lower(adviser.fullName) like lower(concat('%', :adviserSearch, '%'))
            or lower(adviser.userCode) like lower(concat('%', :adviserSearch, '%')))
        ))
      """)
  Page<Topic> findAllByRegistrationPeriodId(
      @Param("registrationPeriodId") Long registrationPeriodId,
      @Param("search") String search,
      @Param("titleFilter") String titleFilter,
      @Param("departmentId") Long departmentId,
      @Param("status") TopicStatus status,
      @Param("lecturerId") Long lecturerId,
      @Param("proposedById") Long proposedById,
      @Param("adviserSearch") String adviserSearch,
      Pageable pageable);

  @Query("select distinct topic from Topic topic join fetch topic.department join fetch topic.registrationPeriod left join fetch topic.advisers left join fetch topic.proposedBy where topic.id in :ids")
  List<Topic> findAllWithAdvisersByIdIn(@Param("ids") List<Long> ids);

  @Query("select count(e) > 0 from Evaluation e where e.topicRegistration.topic.id = :topicId and e.status = com.nhom11.qldt.feature.council.model.enums.EvaluationStatus.SUBMITTED")
  boolean hasSubmittedEvaluation(@Param("topicId") Long topicId);

  @Query("select count(member) > 0 from CouncilMember member where member.council.id = :councilId and member.lecturer.id in :adviserIds")
  boolean hasCouncilMemberIn(@Param("councilId") Long councilId, @Param("adviserIds") java.util.Set<Long> adviserIds);

  @EntityGraph(attributePaths = {"department", "advisers", "registrationPeriod", "proposedBy"})
  Optional<Topic> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  @Query(
      """
      select distinct topic from Topic topic
      left join fetch topic.department
      left join fetch topic.advisers
      left join fetch topic.reviewer
      where topic.registrationPeriod.id = :periodId
      order by topic.title
      """)
  List<Topic> findAllWithDetailsByRegistrationPeriodId(@Param("periodId") Long periodId);

  @Query("select new com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary(topic.id, topic.registrationPeriod.id, topic.title, topic.status) from Topic topic where topic.registrationPeriod.id in :periodIds order by topic.title")
  List<TopicProposalSummary> findProposalSummariesByPeriodIds(@Param("periodIds") List<Long> periodIds);

  @Query("select new com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary(topic.id, topic.registrationPeriod.id, topic.title, topic.status) from Topic topic where topic.registrationPeriod.id in :periodIds and (topic.status = com.nhom11.qldt.feature.topic.model.enums.TopicStatus.APPROVED or topic.proposedBy.id = :lecturerId) order by topic.title")
  List<TopicProposalSummary> findVisibleProposalSummariesByPeriodIds(
      @Param("periodIds") List<Long> periodIds, @Param("lecturerId") Long lecturerId);

  @Query("select new com.nhom11.qldt.feature.topic.model.dto.TopicProposalSummary(topic.id, topic.registrationPeriod.id, topic.title, topic.status) from Topic topic where topic.registrationPeriod.id in :periodIds and topic.proposedBy.id = :lecturerId order by topic.title")
  List<TopicProposalSummary> findProposalSummariesByPeriodIdsAndProposedById(
      @Param("periodIds") List<Long> periodIds, @Param("lecturerId") Long lecturerId);

  List<Topic> findAllByIdInAndRegistrationPeriodId(List<Long> ids, Long registrationPeriodId);

  boolean existsByDepartmentId(Long departmentId);

  boolean existsByCouncilId(Long councilId);

  @Query(
      """
      select distinct topic from Topic topic
      left join fetch topic.department
      left join fetch topic.advisers
      where topic.registrationPeriod.id = :periodId
        and topic.status = :status
        and (:search is null or lower(topic.title) like lower(concat('%', :search, '%')))
        and (:departmentId is null or topic.department.id = :departmentId)
      order by topic.title
      """)
  List<Topic> findAllByRegistrationPeriodIdAndStatus(
      @Param("periodId") Long periodId,
      @Param("status") TopicStatus status,
      @Param("search") String search,
      @Param("departmentId") Long departmentId);
}
