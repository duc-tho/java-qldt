package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import com.nhom11.qldt.feature.council.model.enums.FinalResultStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng hội đồng, đánh giá và công bố kết quả. */
public interface FinalResultRepository extends BaseRepository<FinalResult, Long> {
  @Query(
      "select result.topicRegistration.studentGroup.registrationPeriod.id, count(result) from FinalResult result where result.topicRegistration.studentGroup.registrationPeriod.id in :periodIds and result.status = :status group by result.topicRegistration.studentGroup.registrationPeriod.id")
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  List<Object[]> countByPeriodIdsAndStatus(
      @Param("periodIds") List<Long> periodIds, @Param("status") FinalResultStatus status);

  @Query(
      "select result.topicRegistration.topic.council.id, count(result) from FinalResult result where result.topicRegistration.topic.registrationPeriod.id = :periodId and result.topicRegistration.topic.council.id in :councilIds and result.status = :status group by result.topicRegistration.topic.council.id")
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  List<Object[]> countByCouncilIdsAndPeriodIdAndStatus(
      @Param("councilIds") List<Long> councilIds,
      @Param("periodId") Long periodId,
      @Param("status") FinalResultStatus status);

  @Query(
      "select result.topicRegistration.id from FinalResult result where result.topicRegistration.topic.registrationPeriod.id = :periodId and result.status = :status")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Long> findRegistrationIdsByPeriodIdAndStatus(
      @Param("periodId") Long periodId, @Param("status") FinalResultStatus status);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<FinalResult> findByTopicRegistrationId(Long registrationId);

  @EntityGraph(attributePaths = {"topicRegistration.studentGroup", "topicRegistration.topic"})
  @Query("select result from FinalResult result where result.topicRegistration.topic.registrationPeriod.id = :periodId and result.topicRegistration.studentGroup.id in :groupIds")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<FinalResult> findAllByPeriodAndGroupIds(
      @Param("periodId") Long periodId, @Param("groupIds") List<Long> groupIds);
}
