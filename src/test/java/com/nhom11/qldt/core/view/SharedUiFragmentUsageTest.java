package com.nhom11.qldt.core.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class SharedUiFragmentUsageTest {
    private static final Path TEMPLATES = Path.of("src/main/resources/templates");
    private static final Path BASE = TEMPLATES.resolve("fragment/base");
    private static final Pattern RAW_BUTTON = Pattern.compile("<button\\b[^>]*>.*?</button>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern RAW_TEXTAREA = Pattern.compile("<textarea\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_TABLE = Pattern.compile("<table\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_AVATAR = Pattern.compile("class=\"avatar avatar-placeholder", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAW_BUTTON_LINK = Pattern.compile(
        "<(?:a|label|summary)\\b[^>]*class=\"[^\"]*(?<![\\w-])btn(?![\\w-])[^\"]*\"",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern RAW_CARD_OR_ALERT = Pattern.compile(
        "<(?:article|section|div|aside|a|form)\\b[^>]*class=\"[^\"]*(?<![\\w-])(?:card|alert)(?![\\w-])[^\"]*\"",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern AMBIGUOUS_SLOT = Pattern.compile("~\\{::(?:header|section|footer)\\}");

    @Test
    void templatesDoNotStartWithByteOrderMark() throws IOException {
        try (var files = Files.walk(TEMPLATES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".html")).toList()) {
                assertThat(Files.readString(file))
                    .as("UTF-8 BOM rendered as text in %s", file)
                    .doesNotStartWith("\uFEFF");
            }
        }
    }

    @Test
    void pagesAndFeatureFragmentsUseSharedButtonsCardsAndAlerts() throws IOException {
        try (var files = Files.walk(TEMPLATES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".html") && !path.startsWith(BASE)).toList()) {
                String template = Files.readString(file);
                boolean navigationActions = file.endsWith(Path.of("fragment/ui/navigation-actions.html"));

                var buttons = RAW_BUTTON.matcher(template);
                while (buttons.find()) {
                    String button = buttons.group();
                    assertThat(file.toString().replace('\\', '/'))
                        .as("custom button outside feature, CRUD or UI in %s", file)
                        .containsPattern("/fragment/(feature|crud|ui)/");
                    assertThat(button).as("custom button in %s", file)
                        .contains("th:fragment=")
                        .contains("type=");
                    if (!navigationActions) {
                        assertThat(button).as("button content in %s", file)
                            .containsAnyOf("fragment/base/button ::", "th:replace=\"${content}\"");
                    }
                }
                assertThat(RAW_TEXTAREA.matcher(template).find()).as("raw textarea in %s", file).isFalse();
                assertThat(RAW_TABLE.matcher(template).find()).as("raw table in %s", file).isFalse();
                assertThat(RAW_AVATAR.matcher(template).find()).as("raw avatar in %s", file).isFalse();
                if (!navigationActions) {
                    assertThat(RAW_BUTTON_LINK.matcher(template).find()).as("raw button link in %s", file).isFalse();
                }
                assertThat(RAW_CARD_OR_ALERT.matcher(template).find()).as("raw card or alert in %s", file).isFalse();
                assertThat(AMBIGUOUS_SLOT.matcher(template).find()).as("ambiguous fragment slot in %s", file).isFalse();
            }
        }
    }
}
