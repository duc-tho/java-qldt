package com.nhom11.qldt.feature.studentgroup.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.studentgroup.model.entity.TopicRegistration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRegistrationRepository extends BaseRepository<TopicRegistration, Long> {
  @EntityGraph(attributePaths = {"studentGroup", "topic", "topic.department", "topic.advisers", "reviewedBy"})
  Optional<TopicRegistration> findByStudentGroupId(Long studentGroupId);

  @Query("""
      select distinct registration from TopicRegistration registration
      join registration.topic topic
      join topic.advisers adviser
      where adviser.id = :lecturerId
      order by registration.updatedAt desc
      """)
  List<TopicRegistration> findAllByAdviserId(@Param("lecturerId") Long lecturerId);

  @Query("""
      select distinct registration from TopicRegistration registration
      join fetch registration.studentGroup studentGroup
      join fetch registration.topic topic
      join topic.advisers adviser
      where registration.id = :registrationId and adviser.id = :lecturerId
      """)
  Optional<TopicRegistration> findByIdAndAdviserId(
      @Param("registrationId") Long registrationId, @Param("lecturerId") Long lecturerId);
}
