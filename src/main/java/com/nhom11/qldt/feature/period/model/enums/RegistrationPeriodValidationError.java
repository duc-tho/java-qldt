package com.nhom11.qldt.feature.period.model.enums;

/** Các mã lỗi kiểm tra mốc thời gian của đợt đăng ký. */
public enum RegistrationPeriodValidationError {
  TIMELINE("registration_periods.validation.timeline.invalid"),
  REVIEWER_DEADLINE("registration_periods.validation.reviewer_deadline.invalid"),
  COUNCIL_DATE("registration_periods.validation.council_date.invalid");

  private final String messageCode;

  RegistrationPeriodValidationError(String messageCode) {
    this.messageCode = messageCode;
  }

  /** Trả về mã thông điệp i18n tương ứng với lỗi validation. */
  public String messageCode() {
    return messageCode;
  }
}
