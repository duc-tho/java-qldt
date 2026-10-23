package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.Evaluation;
import com.nhom11.qldt.feature.council.model.enums.EvaluationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

/** Truy cập và truy vấn dữ liệu của chức năng hội đồng, đánh giá và công bố kết quả. */
public interface EvaluationRepository extends BaseRepository<Evaluation, Long> {
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<Evaluation> findByTopicRegistrationIdAndEvaluatorId(Long registrationId, Long evaluatorId);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Evaluation> findAllByTopicRegistrationIdAndStatus(Long registrationId, EvaluationStatus status);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Evaluation> findAllByTopicRegistrationId(Long registrationId);

  @EntityGraph(attributePaths = "topicRegistration")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Evaluation> findAllByTopicRegistrationIdInAndEvaluatorId(
      List<Long> registrationIds, Long evaluatorId);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByTopicRegistrationId(Long registrationId);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByTopicRegistrationTopicCouncilId(Long councilId);
}
