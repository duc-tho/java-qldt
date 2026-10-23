package com.nhom11.qldt.core.validation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
/** Chuyển constraint Jakarta Validation trên DTO thành luật kiểm tra dành cho trình duyệt. */
public class ClientValidationRuleRenderer {
  private final ObjectMapper objectMapper;
  private final MessageSource messageSource;

  public ClientValidationRuleRenderer(ObjectMapper objectMapper, MessageSource messageSource) {
    this.objectMapper = objectMapper;
    this.messageSource = messageSource;
  }

  /** Kết xuất luật validation của biểu mẫu mà không bổ sung luật tùy chỉnh. */
  public Map<String, String> render(Class<?> formType) {
    return render(formType, Map.of());
  }

  /** Kết xuất luật validation theo từng trường và hợp nhất với các luật bổ sung. */
  public Map<String, String> render(
      Class<?> formType, Map<String, List<Map<String, Object>>> extraRules) {
    Map<String, String> validationRules = new LinkedHashMap<>();

    // Luật tùy chỉnh được đặt trước, sau đó mới ghép các constraint khai báo trên DTO.
    for (Field field : formType.getDeclaredFields()) {
      List<Map<String, Object>> rules =
          new ArrayList<>(extraRules.getOrDefault(field.getName(), List.of()));
      rules.addAll(createRules(field));
      validationRules.put(field.getName(), serializeRules(rules));
    }

    return validationRules;
  }

  /** Tạo luật bắt buộc nhập mật khẩu với thông báo được cung cấp. */
  public static List<Map<String, Object>> createPasswordRule(String message) {
    List<Map<String, Object>> rules = new ArrayList<>();
    rules.add(rule("required", message));

    return rules;
  }

  /** Dịch message template dạng mã i18n theo locale hiện tại. */
  private String resolveMessage(String messageTemplate) {
    if (!messageTemplate.startsWith("{") || !messageTemplate.endsWith("}")) {
      return messageTemplate;
    }

    String messageCode = messageTemplate.substring(1, messageTemplate.length() - 1);
    return messageSource.getMessage(
        messageCode, null, messageTemplate, LocaleContextHolder.getLocale());
  }

  /** Tuần tự hóa danh sách luật thành JSON để nhúng vào thuộc tính HTML. */
  private String serializeRules(List<Map<String, Object>> rules) {
    try {
      return objectMapper.writeValueAsString(rules);
    } catch (JacksonException exception) {
      throw new IllegalStateException("Cannot serialize validation rules", exception);
    }
  }

  /** Ánh xạ các constraint được hỗ trợ của một trường sang luật phía client. */
  private List<Map<String, Object>> createRules(Field field) {
    List<Map<String, Object>> rules = new ArrayList<>();

    // Chỉ ánh xạ những constraint mà thư viện validation phía trình duyệt hỗ trợ.
    for (Annotation annotation : field.getAnnotations()) {
      switch (annotation) {
        case NotBlank c -> rules.add(rule("required", resolveMessage(c.message())));
        case NotEmpty c -> rules.add(rule("required", resolveMessage(c.message())));
        case NotNull c -> rules.add(rule("required", resolveMessage(c.message())));
        case Email c -> rules.add(rule("email", resolveMessage(c.message())));
        case Min c -> rules.add(rule("minNumber", c.value(), resolveMessage(c.message())));
        case Max c -> rules.add(rule("maxNumber", c.value(), resolveMessage(c.message())));
        case Pattern c -> rules.add(rule("customRegexp", c.regexp(), resolveMessage(c.message())));

        case DecimalMin c when c.inclusive() ->
            rules.add(rule("minNumber", c.value(), resolveMessage(c.message())));
        case DecimalMax c when c.inclusive() ->
            rules.add(rule("maxNumber", c.value(), resolveMessage(c.message())));

        case Size c -> {
          if (c.min() > 0) rules.add(rule("minLength", c.min(), resolveMessage(c.message())));
          if (c.max() < Integer.MAX_VALUE)
            rules.add(rule("maxLength", c.max(), resolveMessage(c.message())));
        }
        default -> {}
      }
    }

    return rules;
  }

  private static Map<String, Object> rule(String rule, String errorMessage) {
    Map<String, Object> values = new LinkedHashMap<>();

    values.put("rule", rule);
    values.put("errorMessage", errorMessage);

    return values;
  }

  private static Map<String, Object> rule(String rule, Object value, String errorMessage) {
    Map<String, Object> values = rule(rule, errorMessage);

    values.put("value", value);

    return values;
  }
}
