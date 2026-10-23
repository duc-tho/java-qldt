package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.ResultPublication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

/** Truy cập và truy vấn dữ liệu của chức năng hội đồng, đánh giá và công bố kết quả. */
public interface ResultPublicationRepository extends BaseRepository<ResultPublication, Long> {
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<ResultPublication> findFirstByRegistrationPeriodIdOrderByIdDesc(Long periodId);

  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<ResultPublication> findAllByRegistrationPeriodIdOrderByIdDesc(Long periodId);

  @EntityGraph(attributePaths = "registrationPeriod")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<ResultPublication> findAllByRegistrationPeriodIdInOrderByIdDesc(List<Long> periodIds);
}
