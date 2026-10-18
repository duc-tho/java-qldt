package com.nhom11.qldt.feature.council.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.council.model.entity.CouncilMember;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouncilMemberRepository extends BaseRepository<CouncilMember, Long> {
  @Query("select member from CouncilMember member join fetch member.lecturer where member.council.id = :councilId")
  List<CouncilMember> findAllByCouncilId(@Param("councilId") Long councilId);

  @Query("select member from CouncilMember member join fetch member.lecturer where member.council.id in :councilIds")
  List<CouncilMember> findAllByCouncilIdIn(@Param("councilIds") List<Long> councilIds);

  boolean existsByCouncilIdAndLecturerId(Long councilId, Long lecturerId);
}
