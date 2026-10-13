package com.nhom11.qldt.core.table;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ActionDefTest {

  @Test
  void overflowMenuAppearsOnlyWhenAnOverflowActionIsVisible() {
    Object item = new Object();
    List<ActionDef> actions =
        List.of(
            new ActionDef.Link("first", "", value -> "#"),
            new ActionDef.Link("second", "", value -> "#"),
            new ActionDef.Link("third", "", value -> "#"),
            new ActionDef.Link("fourth", "", value -> "#", value -> false));

    assertThat(ActionDef.hasVisibleOverflow(actions, item)).isFalse();
    assertThat(
            ActionDef.hasVisibleOverflow(
                List.of(
                    actions.get(0),
                    actions.get(1),
                    actions.get(2),
                    new ActionDef.Link("fourth", "", value -> "#", value -> true)),
                item))
        .isTrue();
  }
}
