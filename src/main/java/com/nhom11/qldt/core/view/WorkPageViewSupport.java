package com.nhom11.qldt.core.view;

import com.nhom11.qldt.feature.period.model.dto.PeriodWorkCard;
import com.nhom11.qldt.feature.period.model.dto.PeriodWorkSection;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
/** Hỗ trợ dựng các trang công việc theo từng đợt và dịch nội dung theo locale hiện tại. */
public class WorkPageViewSupport {
  private final MessageSource messageSource;

  public WorkPageViewSupport(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /** Tạo một nhóm thẻ công việc với tiêu đề và thông báo rỗng đã được dịch. */
  public PeriodWorkSection section(
      String id, String titleKey, String emptyKey, List<PeriodWorkCard> cards) {
    return new PeriodWorkSection(id, message(titleKey), message(emptyKey), cards);
  }

  /** Gắn metadata tiêu đề, mô tả và biểu tượng của trang vào model. */
  public void page(Model model, String title, String description, String icon) {
    model.addAttribute("pageTitle", title);
    model.addAttribute("pageDesc", description);
    model.addAttribute("pageIcon", icon);
  }

  /** Tra cứu thông điệp i18n theo locale của yêu cầu hiện tại. */
  public String message(String code, Object... arguments) {
    return messageSource.getMessage(code, arguments, LocaleContextHolder.getLocale());
  }
}
