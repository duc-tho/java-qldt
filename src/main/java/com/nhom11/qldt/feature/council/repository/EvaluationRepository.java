package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.Evaluation;
import com.nhom11.qldt.feature.council.model.enums.EvaluationStatus;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends BaseRepository<Evaluation, Long> {
  Optional<Evaluation> findByTopicRegistrationIdAndEvaluatorId(Long registrationId, Long evaluatorId);

  List<Evaluation> findAllByTopicRegistrationIdAndStatus(Long registrationId, EvaluationStatus status);

  List<Evaluation> findAllByTopicRegistrationId(Long registrationId);

  boolean existsByTopicRegistrationId(Long registrationId);

  boolean existsByTopicRegistrationTopicCouncilId(Long councilId);
}
