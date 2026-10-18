package com.nhom11.qldt.feature.period.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.attoparser.AbstractMarkupHandler;
import org.attoparser.MarkupParser;
import org.attoparser.config.ParseConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RegistrationPeriodFieldsTemplateTest {
  @Test
  void registrationPeriodFieldsHaveValidMarkup() {
    ClassPathResource template =
        new ClassPathResource("templates/fragment/feature/period/fields.html");
    ParseConfiguration configuration = ParseConfiguration.htmlConfiguration();
    configuration.setUniqueAttributesInElementRequired(true);

    assertDoesNotThrow(
        () -> {
          try (InputStreamReader reader =
              new InputStreamReader(template.getInputStream(), StandardCharsets.UTF_8)) {
            new MarkupParser(configuration).parse(reader, new AbstractMarkupHandler() {});
          }
        });
  }
}
