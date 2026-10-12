package com.nhom11.qldt.feature.auth.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.auth.model.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends BaseRepository<User, Long> {
  @EntityGraph(attributePaths = "roles")
  Optional<User> findByEmail(String email);

  @EntityGraph(attributePaths = "roles")
  Optional<User> findByUserCode(String userCode);

  @Query(
      """
      select distinct user from User user join user.roles role
      where role.name = 'LECTURER'
        and user.id in :ids
      """)
  List<User> findAllLecturersByIdIn(@Param("ids") Set<Long> ids);

  @Query(
      """
      select distinct user from User user join user.roles role
      where role.name = 'LECTURER'
      order by user.fullName
      """)
  List<User> findAllLecturers();

  @Query("select distinct user from User user join user.roles role where role.name = 'LECTURER' and (:search is null or lower(user.fullName) like lower(concat('%', :search, '%')) or lower(user.userCode) like lower(concat('%', :search, '%'))) order by user.fullName")
  List<User> searchLecturers(@Param("search") String search, Pageable pageable);
}
