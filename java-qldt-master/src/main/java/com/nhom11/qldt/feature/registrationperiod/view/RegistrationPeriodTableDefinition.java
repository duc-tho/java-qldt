package com.nhom11.qldt.feature.registrationperiod.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RegistrationPeriodTableDefinition extends BaseTableDefinition<RegistrationPeriod> {

  private final MessageSource messageSource;

  public RegistrationPeriodTableDefinition(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public List<ColumnDef> columns() {
    return List.of(
        new ColumnDef(
            "id",
            "registration_periods.column.id",
            item -> String.valueOf(((RegistrationPeriod) item).getId()),
            false,
            false,
            true),
        new ColumnDef(
            "name",
            "registration_periods.column.name",
            item -> ((RegistrationPeriod) item).getName(),
            true,
            true,
            true),
        new ColumnDef(
            "periodType",
            "registration_periods.column.type",
            item -> periodTypeLabel((RegistrationPeriod) item),
            false,
            false,
            true));
  }

  @Override
  public List<ActionDef> actions(String baseUrl) {
    return List.of(
        new ActionDef.Link(
            "registration_periods.action.edit",
            "bi:pencil-square",
            item -> baseUrl + "/" + ((RegistrationPeriod) item).getId() + "/edit"),
        new ActionDef.Delete(
            "registration_periods.action.delete",
            "bi:trash",
            item -> baseUrl + "/" + ((RegistrationPeriod) item).getId(),
            item -> ((RegistrationPeriod) item).getName()));
  }

  private String periodTypeLabel(RegistrationPeriod period) {
    return messageSource.getMessage(
        "period-types." + period.getPeriodType().name(), null, LocaleContextHolder.getLocale());
  }
}
