package com.nhom11.qldt.feature.announcement.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.announcement.model.dto.AnnouncementSummary;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng thông báo. */
public interface AnnouncementRepository extends BaseRepository<Announcement, Long> {
  @EntityGraph(attributePaths = "roles")
  @Query(
      "select distinct announcement from Announcement announcement join announcement.roles role "
          + "where announcement.status = com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus.PUBLISHED "
          + "and role.name in :roleNames order by announcement.createdAt desc")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<Announcement> findPublishedForRoles(@Param("roleNames") Set<String> roleNames);

  @Query(
      "select distinct new com.nhom11.qldt.feature.announcement.model.dto.AnnouncementSummary(announcement.id, announcement.title, announcement.createdAt) "
          + "from Announcement announcement join announcement.roles role "
          + "where announcement.status = com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus.PUBLISHED "
          + "and role.name in :roleNames order by announcement.createdAt desc")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<AnnouncementSummary> findRecentPublishedForRoles(
      @Param("roleNames") Set<String> roleNames, Pageable pageable);

  @EntityGraph(attributePaths = "roles")
  @Query(
      "select distinct announcement from Announcement announcement join announcement.roles role "
          + "where announcement.id = :id "
          + "and announcement.status = com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus.PUBLISHED "
          + "and role.name in :roleNames")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<Announcement> findPublishedByIdForRoles(
      @Param("id") Long id, @Param("roleNames") Set<String> roleNames);
}
