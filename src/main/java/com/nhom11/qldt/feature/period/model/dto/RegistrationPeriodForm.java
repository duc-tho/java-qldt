package com.nhom11.qldt.feature.period.model.dto;

import com.nhom11.qldt.core.form.CrudForm;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.model.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
/** Nhận loại đợt và các mốc thời gian từ form tạo hoặc chỉnh sửa đợt đăng ký. */
public class RegistrationPeriodForm implements CrudForm<RegistrationPeriod> {
  private Long id;

  @NotBlank(message = "{registration_periods.validation.name.required}")
  private String name;

  @NotNull(message = "{registration_periods.validation.type.required}")
  private PeriodType periodType;

  @NotNull(message = "{registration_periods.validation.lecturer_registration_start.required}")
  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime lecturerRegistrationStart;

  @NotNull(message = "{registration_periods.validation.lecturer_registration_end.required}")
  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime lecturerRegistrationEnd;

  @NotNull(message = "{registration_periods.validation.student_registration_start.required}")
  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime studentRegistrationStart;

  @NotNull(message = "{registration_periods.validation.student_registration_end.required}")
  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime studentRegistrationEnd;

  @NotNull(message = "{registration_periods.validation.report_deadline.required}")
  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime reportDeadline;

  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime reviewerDeadline;

  @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
  private LocalDateTime councilDate;

  /** Chuyển dữ liệu biểu mẫu thành thực thể để tầng dịch vụ xử lý. */
  public RegistrationPeriod toEntity() {

    RegistrationPeriod item = new RegistrationPeriod();
    item.setName(name);
    item.setPeriodType(periodType);
    item.setLecturerRegistrationStart(lecturerRegistrationStart);
    item.setLecturerRegistrationEnd(lecturerRegistrationEnd);
    item.setStudentRegistrationStart(studentRegistrationStart);
    item.setStudentRegistrationEnd(studentRegistrationEnd);
    item.setReportDeadline(reportDeadline);
    item.setReviewerDeadline(reviewerDeadline);
    item.setCouncilDate(councilDate);

    return item;
  }

  /** Điền dữ liệu thực thể vào biểu mẫu để hiển thị hoặc chỉnh sửa. */
  public void fromEntity(RegistrationPeriod item) {
    id = item.getId();
    name = item.getName();
    periodType = item.getPeriodType();
    lecturerRegistrationStart = item.getLecturerRegistrationStart();
    lecturerRegistrationEnd = item.getLecturerRegistrationEnd();
    studentRegistrationStart = item.getStudentRegistrationStart();
    studentRegistrationEnd = item.getStudentRegistrationEnd();
    reportDeadline = item.getReportDeadline();
    reviewerDeadline = item.getReviewerDeadline();
    councilDate = item.getCouncilDate();
  }
}
