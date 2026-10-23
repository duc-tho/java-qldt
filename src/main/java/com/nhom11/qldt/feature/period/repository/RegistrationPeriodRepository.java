package com.nhom11.qldt.feature.period.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy cập và truy vấn dữ liệu của chức năng đợt đăng ký. */
public interface RegistrationPeriodRepository extends BaseRepository<RegistrationPeriod, Long> {
  @Query(
      """
      select count(period) from RegistrationPeriod period
      where period.periodType = com.nhom11.qldt.feature.period.model.enums.PeriodType.KLTN
        and period.studentRegistrationEnd < :now
        and period.councilDate > :now
      """)
  /** Đếm số bản ghi thỏa các điều kiện được thể hiện bởi tham số truy vấn. */
  long countOpenCouncilPeriods(@Param("now") LocalDateTime now);

  @Query(
      """
      select period from RegistrationPeriod period
      where period.studentRegistrationStart <= :now
        and period.studentRegistrationEnd >= :now
      order by period.studentRegistrationEnd asc
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<RegistrationPeriod> findOpenForStudents(@Param("now") LocalDateTime now);

  @Query(
      """
      select period from RegistrationPeriod period
      where period.lecturerRegistrationStart <= :now
        and period.studentRegistrationEnd >= :now
      order by period.studentRegistrationEnd asc
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<RegistrationPeriod> findVisibleForLecturers(@Param("now") LocalDateTime now);

  @Query(
      """
      select period from RegistrationPeriod period
      where period.lecturerRegistrationStart <= :now
        and (period.studentRegistrationEnd >= :now
          or exists (select topic.id from Topic topic left join topic.advisers adviser
            where topic.registrationPeriod = period
              and (topic.proposedBy.id = :lecturerId or adviser.id = :lecturerId
                or topic.reviewer.id = :lecturerId))
          or exists (select member.id from CouncilMember member
            where member.council.registrationPeriod = period and member.lecturer.id = :lecturerId))
      order by period.lecturerRegistrationEnd desc
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<RegistrationPeriod> findVisibleForLecturer(
      @Param("now") LocalDateTime now, @Param("lecturerId") Long lecturerId);

  @Query(
      value =
          """
          select period from RegistrationPeriod period
          where period.lecturerRegistrationStart <= :now
            and (period.studentRegistrationEnd >= :now
              or exists (select topic.id from Topic topic left join topic.advisers adviser
                where topic.registrationPeriod = period
                  and (topic.proposedBy.id = :lecturerId or adviser.id = :lecturerId
                    or topic.reviewer.id = :lecturerId))
              or exists (select member.id from CouncilMember member
                where member.council.registrationPeriod = period and member.lecturer.id = :lecturerId))
            and (:search is null or lower(period.name) like lower(concat('%', :search, '%')))
            and (:nameFilter is null or lower(period.name) like lower(concat('%', :nameFilter, '%')))
          """,
      countQuery =
          """
          select count(period) from RegistrationPeriod period
          where period.lecturerRegistrationStart <= :now
            and (period.studentRegistrationEnd >= :now
              or exists (select topic.id from Topic topic left join topic.advisers adviser
                where topic.registrationPeriod = period
                  and (topic.proposedBy.id = :lecturerId or adviser.id = :lecturerId
                    or topic.reviewer.id = :lecturerId))
              or exists (select member.id from CouncilMember member
                where member.council.registrationPeriod = period and member.lecturer.id = :lecturerId))
            and (:search is null or lower(period.name) like lower(concat('%', :search, '%')))
            and (:nameFilter is null or lower(period.name) like lower(concat('%', :nameFilter, '%')))
          """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Page<RegistrationPeriod> findVisibleForLecturer(
      @Param("now") LocalDateTime now,
      @Param("lecturerId") Long lecturerId,
      @Param("search") String search,
      @Param("nameFilter") String nameFilter,
      Pageable pageable);

  @Query(
      """
      select period from RegistrationPeriod period
      where (period.studentRegistrationStart <= :now and period.studentRegistrationEnd >= :now)
        or exists (select member.id from GroupMember member
          where member.registrationPeriod = period and member.student.id = :studentId)
      order by period.studentRegistrationEnd desc
      """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  List<RegistrationPeriod> findVisibleForStudent(
      @Param("now") LocalDateTime now, @Param("studentId") Long studentId);

  @Query(
      value =
          """
          select period from RegistrationPeriod period
          where ((period.studentRegistrationStart <= :now and period.studentRegistrationEnd >= :now)
            or exists (select member.id from GroupMember member
              where member.registrationPeriod = period and member.student.id = :studentId))
            and (:search is null or lower(period.name) like lower(concat('%', :search, '%')))
            and (:nameFilter is null or lower(period.name) like lower(concat('%', :nameFilter, '%')))
          """,
      countQuery =
          """
          select count(period) from RegistrationPeriod period
          where ((period.studentRegistrationStart <= :now and period.studentRegistrationEnd >= :now)
            or exists (select member.id from GroupMember member
              where member.registrationPeriod = period and member.student.id = :studentId))
            and (:search is null or lower(period.name) like lower(concat('%', :search, '%')))
            and (:nameFilter is null or lower(period.name) like lower(concat('%', :nameFilter, '%')))
          """)
  /** Truy vấn các bản ghi phù hợp với phạm vi và điều kiện được cung cấp. */
  Page<RegistrationPeriod> findVisibleForStudent(
      @Param("now") LocalDateTime now,
      @Param("studentId") Long studentId,
      @Param("search") String search,
      @Param("nameFilter") String nameFilter,
      Pageable pageable);
}
