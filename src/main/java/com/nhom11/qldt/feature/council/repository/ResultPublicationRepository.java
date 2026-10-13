package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.ResultPublication;
import java.util.List;
import java.util.Optional;

public interface ResultPublicationRepository extends BaseRepository<ResultPublication, Long> {
  Optional<ResultPublication> findFirstByRegistrationPeriodIdOrderByIdDesc(Long periodId);

  List<ResultPublication> findAllByRegistrationPeriodIdOrderByIdDesc(Long periodId);
}
