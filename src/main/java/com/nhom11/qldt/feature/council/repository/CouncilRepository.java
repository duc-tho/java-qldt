package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.Council;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouncilRepository extends BaseRepository<Council, Long> {
  long countByRegistrationPeriodId(Long registrationPeriodId);

  @Query("select council from Council council where council.registrationPeriod.id = :periodId order by council.name")
  List<Council> findAllByRegistrationPeriodId(@Param("periodId") Long periodId);

  Optional<Council> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);
}
