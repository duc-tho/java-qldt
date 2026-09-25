package com.nhom11.qldt.feature.registrationperiod.model.enums;

public enum RegistrationPeriodValidationError {
  TIMELINE("registration_periods.validation.timeline.invalid"),
  REVIEWER_DEADLINE("registration_periods.validation.reviewer_deadline.invalid"),
  COUNCIL_DATE("registration_periods.validation.council_date.invalid");

  private final String messageCode;

  RegistrationPeriodValidationError(String messageCode) {
    this.messageCode = messageCode;
  }

  public String messageCode() {
    return messageCode;
  }
}
