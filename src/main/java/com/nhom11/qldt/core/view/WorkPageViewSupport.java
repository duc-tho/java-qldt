package com.nhom11.qldt.core.view;

import com.nhom11.qldt.feature.period.model.dto.PeriodWorkCard;
import com.nhom11.qldt.feature.period.model.dto.PeriodWorkSection;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class WorkPageViewSupport {
  private final MessageSource messageSource;

  public WorkPageViewSupport(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public PeriodWorkSection section(
      String id, String titleKey, String emptyKey, List<PeriodWorkCard> cards) {
    return new PeriodWorkSection(id, message(titleKey), message(emptyKey), cards);
  }

  public void page(Model model, String title, String description, String icon) {
    model.addAttribute("pageTitle", title);
    model.addAttribute("pageDesc", description);
    model.addAttribute("pageIcon", icon);
  }

  public String message(String code, Object... arguments) {
    return messageSource.getMessage(code, arguments, LocaleContextHolder.getLocale());
  }
}
