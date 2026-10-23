package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.CouncilMember;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng hội đồng, đánh giá và công bố kết quả. */
public interface CouncilMemberRepository extends BaseRepository<CouncilMember, Long> {
  @Query("select member from CouncilMember member join fetch member.lecturer where member.council.id = :councilId")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<CouncilMember> findAllByCouncilId(@Param("councilId") Long councilId);

  @Query("select member from CouncilMember member join fetch member.lecturer where member.council.id in :councilIds")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<CouncilMember> findAllByCouncilIdIn(@Param("councilIds") List<Long> councilIds);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByCouncilIdAndLecturerId(Long councilId, Long lecturerId);
}
