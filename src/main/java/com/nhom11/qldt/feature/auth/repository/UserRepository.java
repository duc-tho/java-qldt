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

/** Truy cập và truy vấn dữ liệu của chức năng tài khoản và người dùng. */
public interface UserRepository extends BaseRepository<User, Long> {
  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByEmail(String email);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByEmailAndIdNot(String email, Long id);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByUserCode(String userCode);

  /** Kiểm tra sự tồn tại của bản ghi thỏa điều kiện nghiệp vụ được cung cấp. */
  boolean existsByUserCodeAndIdNot(String userCode, Long id);

  @EntityGraph(attributePaths = "roles")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<User> findByEmail(String email);

  @EntityGraph(attributePaths = "roles")
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Optional<User> findByUserCode(String userCode);

  @Query(
      """
      select distinct user from User user join user.roles role
      where role.name = 'LECTURER'
        and user.id in :ids
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<User> findAllLecturersByIdIn(@Param("ids") Set<Long> ids);

  @Query(
      """
      select distinct user from User user join user.roles role
      where role.name = 'LECTURER'
      order by user.fullName
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<User> findAllLecturers();

  @Query("select distinct user from User user join user.roles role where role.name = 'LECTURER' and (:search is null or lower(user.fullName) like lower(concat('%', :search, '%')) or lower(user.userCode) like lower(concat('%', :search, '%'))) order by user.fullName")
  /** Thực hiện truy vấn searchLecturers trên dữ liệu của chức năng. */
  List<User> searchLecturers(@Param("search") String search, Pageable pageable);
}
