package com.nhom11.qldt.feature.announcement.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.announcement.model.entity.Announcement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends BaseRepository<Announcement, Long> {
  @EntityGraph(attributePaths = "roles")
  @Query(
      "select distinct announcement from Announcement announcement join announcement.roles role "
          + "where announcement.status = com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus.PUBLISHED "
          + "and role.name in :roleNames order by announcement.createdAt desc")
  List<Announcement> findPublishedForRoles(@Param("roleNames") Set<String> roleNames);

  @EntityGraph(attributePaths = "roles")
  @Query(
      "select distinct announcement from Announcement announcement join announcement.roles role "
          + "where announcement.id = :id "
          + "and announcement.status = com.nhom11.qldt.feature.announcement.model.enums.AnnouncementStatus.PUBLISHED "
          + "and role.name in :roleNames")
  Optional<Announcement> findPublishedByIdForRoles(
      @Param("id") Long id, @Param("roleNames") Set<String> roleNames);
}
