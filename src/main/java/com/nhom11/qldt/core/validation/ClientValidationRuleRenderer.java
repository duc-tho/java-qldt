package com.nhom11.qldt.core.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ClientValidationRuleRenderer {
    private final ObjectMapper objectMapper;

    public ClientValidationRuleRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> render(Class<?> formType) {
        Map<String, String> validationRules = new LinkedHashMap<>();

        for (Field field : formType.getDeclaredFields()) {
            validationRules.put(field.getName(), serializeRules(field));
        }

        return validationRules;
    }

    private String serializeRules(Field field) {
        try {
            return objectMapper.writeValueAsString(createRules(field));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Cannot serialize validation rules for field: " + field.getName(),
                    exception);
        }
    }

    private List<Map<String, Object>> createRules(Field field) {
        List<Map<String, Object>> rules = new ArrayList<>();

        for (Annotation annotation : field.getAnnotations()) {
            switch (annotation) {
                case NotBlank c -> rules.add(rule("required", c.message()));
                case NotEmpty c -> rules.add(rule("required", c.message()));
                case NotNull c -> rules.add(rule("required", c.message()));
                case Email c -> rules.add(rule("email", c.message()));
                case Min c -> rules.add(rule("minNumber", c.value(), c.message()));
                case Max c -> rules.add(rule("maxNumber", c.value(), c.message()));
                case Pattern c -> rules.add(rule("customRegexp", c.regexp(), c.message()));

                case DecimalMin c when c.inclusive() -> rules.add(rule("minNumber", c.value(), c.message()));
                case DecimalMax c when c.inclusive() -> rules.add(rule("maxNumber", c.value(), c.message()));

                case Size c -> {
                    if (c.min() > 0)
                        rules.add(rule("minLength", c.min(), c.message()));
                    if (c.max() < Integer.MAX_VALUE)
                        rules.add(rule("maxLength", c.max(), c.message()));
                }
                default -> {
                }
            }
        }

        return rules;
    }

    private Map<String, Object> rule(String rule, String errorMessage) {
        Map<String, Object> values = new LinkedHashMap<>();

        values.put("rule", rule);
        values.put("errorMessage", errorMessage);

        return values;
    }

    private Map<String, Object> rule(String rule, Object value, String errorMessage) {
        Map<String, Object> values = rule(rule, errorMessage);

        values.put("value", value);

        return values;
    }
}
