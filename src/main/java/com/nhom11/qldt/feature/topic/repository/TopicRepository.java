package com.nhom11.qldt.feature.topic.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.topic.model.entity.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRepository extends BaseRepository<Topic, Long> {
  @Query(
      """
      select topic from Topic topic
      where topic.registrationPeriod.id = :registrationPeriodId
        and (:search is null or lower(topic.title) like lower(concat('%', :search, '%')))
        and (:titleFilter is null or lower(topic.title) like lower(concat('%', :titleFilter, '%')))
      """)
  Page<Topic> findAllByRegistrationPeriodId(
      @Param("registrationPeriodId") Long registrationPeriodId,
      @Param("search") String search,
      @Param("titleFilter") String titleFilter,
      Pageable pageable);

  Optional<Topic> findByIdAndRegistrationPeriodId(Long id, Long registrationPeriodId);

  List<Topic> findAllByIdInAndRegistrationPeriodId(List<Long> ids, Long registrationPeriodId);
}
