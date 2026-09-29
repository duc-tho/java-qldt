package com.nhom11.qldt.feature.registrationperiod.repository;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationPeriodRepository extends BaseRepository<RegistrationPeriod, Long> {
  @Query("""
      select period from RegistrationPeriod period
      where period.studentRegistrationStart <= :now
        and period.studentRegistrationEnd >= :now
      order by period.studentRegistrationEnd asc
      """)
  List<RegistrationPeriod> findOpenForStudents(@Param("now") LocalDateTime now);

  @Query("""
      select period from RegistrationPeriod period
      where period.lecturerRegistrationStart <= :now
        and period.studentRegistrationEnd >= :now
      order by period.studentRegistrationEnd asc
      """)
  List<RegistrationPeriod> findVisibleForLecturers(@Param("now") LocalDateTime now);
}
