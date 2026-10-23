package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.Council;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng hội đồng, đánh giá và công bố kết quả. */
public interface CouncilRepository extends BaseRepository<Council, Long> {
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  long countByRegistrationPeriodId(Long registrationPeriodId);

  @Query("select council.registrationPeriod.id, count(council) from Council council where council.registrationPeriod.id in :periodIds group by council.registrationPeriod.id")
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  List<Object[]> countByRegistrationPeriodIdIn(@Param("periodIds") List<Long> periodIds);

  @Query("select council from Council council where council.registrationPeriod.id = :periodId order by council.name")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Council> findAllByRegistrationPeriodId(@Param("periodId") Long periodId);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<Council> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);
}
