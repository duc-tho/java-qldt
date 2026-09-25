package com.nhom11.qldt.feature.registrationperiod.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.registrationperiod.model.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class RegistrationPeriodForm implements CrudForm<RegistrationPeriod> {
  private Long id;
  @NotBlank private String name;
  @NotNull private PeriodType periodType;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime lecturerRegistrationStart;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime lecturerRegistrationEnd;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime topicPublicationAt;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime studentRegistrationStart;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime studentRegistrationEnd;

  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime reportDeadline;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime reviewerDeadline;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime councilDate;

  public RegistrationPeriod toEntity() {

    RegistrationPeriod item = new RegistrationPeriod();
    item.setId(id);
    item.setName(name);
    item.setPeriodType(periodType);
    item.setLecturerRegistrationStart(lecturerRegistrationStart);
    item.setLecturerRegistrationEnd(lecturerRegistrationEnd);
    item.setTopicPublicationAt(topicPublicationAt);
    item.setStudentRegistrationStart(studentRegistrationStart);
    item.setStudentRegistrationEnd(studentRegistrationEnd);
    item.setReportDeadline(reportDeadline);
    item.setReviewerDeadline(reviewerDeadline);
    item.setCouncilDate(councilDate);

    return item;
  }

  public void fromEntity(RegistrationPeriod item) {
    id = item.getId();
    name = item.getName();
    periodType = item.getPeriodType();
    lecturerRegistrationStart = item.getLecturerRegistrationStart();
    lecturerRegistrationEnd = item.getLecturerRegistrationEnd();
    topicPublicationAt = item.getTopicPublicationAt();
    studentRegistrationStart = item.getStudentRegistrationStart();
    studentRegistrationEnd = item.getStudentRegistrationEnd();
    reportDeadline = item.getReportDeadline();
    reviewerDeadline = item.getReviewerDeadline();
    councilDate = item.getCouncilDate();
  }
}
