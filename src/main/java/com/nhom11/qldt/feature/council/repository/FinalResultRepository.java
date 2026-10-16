package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.council.model.enums.FinalResultStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinalResultRepository extends BaseRepository<FinalResult, Long> {
  @Query(
      "select result.topicRegistration.studentGroup.registrationPeriod.id, count(result) from FinalResult result where result.topicRegistration.studentGroup.registrationPeriod.id in :periodIds and result.status = :status group by result.topicRegistration.studentGroup.registrationPeriod.id")
  List<Object[]> countByPeriodIdsAndStatus(
      @Param("periodIds") List<Long> periodIds, @Param("status") FinalResultStatus status);

  Optional<FinalResult> findByTopicRegistrationId(Long registrationId);

  @EntityGraph(attributePaths = {"topicRegistration.studentGroup", "topicRegistration.topic"})
  @Query("select result from FinalResult result where result.topicRegistration.topic.registrationPeriod.id = :periodId and result.topicRegistration.studentGroup.id in :groupIds")
  List<FinalResult> findAllByPeriodAndGroupIds(
      @Param("periodId") Long periodId, @Param("groupIds") List<Long> groupIds);
}
