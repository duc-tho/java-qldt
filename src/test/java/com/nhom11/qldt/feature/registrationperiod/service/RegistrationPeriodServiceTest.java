package com.nhom11.qldt.feature.registrationperiod.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.repository.RegistrationPeriodRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {
  @Mock private RegistrationPeriodRepository repository;

  @InjectMocks private RegistrationPeriodService service;

  @Test
  void findOpenForStudentsReturnsOnlyPeriodsProvidedByOpenWindowQuery() {
    RegistrationPeriod period = new RegistrationPeriod();
    List<RegistrationPeriod> expected = List.of(period);
    when(repository.findOpenForStudents(any(LocalDateTime.class))).thenReturn(expected);

    List<RegistrationPeriod> actual = service.findOpenForStudents();

    assertThat(actual).containsExactly(period);
    verify(repository).findOpenForStudents(any(LocalDateTime.class));
  }
}
