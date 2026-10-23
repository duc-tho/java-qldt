package com.nhom11.qldt.feature.period.view;

import com.nhom11.qldt.core.table.ActionDef;
import com.nhom11.qldt.core.table.BaseTableDefinition;
import com.nhom11.qldt.core.table.ColumnDef;
import com.nhom11.qldt.feature.period.model.entity.RegistrationPeriod;
import com.nhom11.qldt.feature.period.service.RegistrationPeriodService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
/** Khai báo cột, thao tác và quyền hiển thị của bảng đợt đăng ký. */
public class RegistrationPeriodTableDefinition extends BaseTableDefinition<RegistrationPeriod> {

  private final MessageSource messageSource;
  private final RegistrationPeriodService service;

  public RegistrationPeriodTableDefinition(
      MessageSource messageSource, RegistrationPeriodService service) {
    this.messageSource = messageSource;
    this.service = service;
  }

  @Override
  /** Khai báo các cột và khả năng tìm kiếm, lọc, sắp xếp của bảng. */
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
            true),
        new ColumnDef(
            "stage",
            "registration_periods.column.stage",
            item -> stageLabel((RegistrationPeriod) item),
            false,
            false,
            false));
  }

  @Override
  /** Khai báo các thao tác hiển thị cho từng bản ghi trong bảng. */
  public List<ActionDef> actions(String baseUrl) {
    if (!canManageRegistrationPeriods()) {
      return List.of(
          new ActionDef.Link(
              "crud.action.detail",
              "bi:eye",
              item -> baseUrl + "/" + ((RegistrationPeriod) item).getId()));
    }
    return List.of(
        new ActionDef.Link(
            "crud.action.detail",
            "bi:eye",
            item -> baseUrl + "/" + ((RegistrationPeriod) item).getId()),
        new ActionDef.Link(
            "crud.action.edit",
            "bi:pencil-square",
            item -> baseUrl + "/" + ((RegistrationPeriod) item).getId() + "/sua"),
        new ActionDef.Delete(
            "crud.action.delete",
            "bi:trash",
            item -> baseUrl + "/" + ((RegistrationPeriod) item).getId() + "/xoa",
            item -> ((RegistrationPeriod) item).getName()));
  }

  @Override
  /** Tạo URL dẫn đến màn hình tạo mới. */
  public String createUrl(String baseUrl) {
    return baseUrl + "/tao";
  }

  @Override
  /** Kiểm tra người dùng hiện tại có quyền tạo mới hay không. */
  public boolean canCreate() {
    return canManageRegistrationPeriods();
  }

  private boolean canManageRegistrationPeriods() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(
            authority ->
                authority.getAuthority().equals("ROLE_HEAD_OF_FACULTY")
                    || authority.getAuthority().equals("ROLE_ADMIN"));
  }

  private String periodTypeLabel(RegistrationPeriod period) {
    return messageSource.getMessage(
        "period-types." + period.getPeriodType().name(), null, LocaleContextHolder.getLocale());
  }

  private String stageLabel(RegistrationPeriod period) {
    return messageSource.getMessage(
        "registration_periods.stage." + service.stage(period, LocalDateTime.now()).name(),
        null,
        LocaleContextHolder.getLocale());
  }
}
