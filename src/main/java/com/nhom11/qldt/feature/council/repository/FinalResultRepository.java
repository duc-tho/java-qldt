package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.FinalResult;
import java.util.Optional;

public interface FinalResultRepository extends BaseRepository<FinalResult, Long> {
  Optional<FinalResult> findByTopicRegistrationId(Long registrationId);
}
