package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.ResultPublication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResultPublicationRepository extends BaseRepository<ResultPublication, Long> {
  @Query("select publication from ResultPublication publication where publication.registrationPeriod.id = :periodId order by publication.performedAt desc")
  Optional<ResultPublication> findFirstByRegistrationPeriodIdOrderByPerformedAtDesc(@Param("periodId") Long periodId);

  List<ResultPublication> findAllByRegistrationPeriodIdOrderByPerformedAtDesc(Long periodId);
}
