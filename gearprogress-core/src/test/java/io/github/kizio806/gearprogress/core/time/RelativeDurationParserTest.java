package io.github.kizio806.gearprogress.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RelativeDurationParserTest {

  @Test
  void testParseSimpleUnits() {
    assertEquals(Duration.ofSeconds(45), RelativeDurationParser.parse("45s"));
    assertEquals(Duration.ofMinutes(10), RelativeDurationParser.parse("10m"));
    assertEquals(Duration.ofHours(3), RelativeDurationParser.parse("3h"));
    assertEquals(Duration.ofDays(7), RelativeDurationParser.parse("7d"));
    assertEquals(Duration.ofDays(14), RelativeDurationParser.parse("2w"));
  }

  @Test
  void testParseCompositeUnits() {
    assertEquals(Duration.ofHours(26), RelativeDurationParser.parse("1d2h"));
    assertEquals(Duration.ofSeconds(3665), RelativeDurationParser.parse("1h 1m 5s"));
  }

  @Test
  void testParseIsoFormat() {
    assertEquals(Duration.ofDays(7), RelativeDurationParser.parse("P7D"));
  }

  @Test
  void testParseInvalid() {
    assertThrows(NullPointerException.class, () -> RelativeDurationParser.parse(null));
    assertThrows(IllegalArgumentException.class, () -> RelativeDurationParser.parse(""));
    assertThrows(IllegalArgumentException.class, () -> RelativeDurationParser.parse("abc"));
    assertThrows(IllegalArgumentException.class, () -> RelativeDurationParser.parse("5x"));
  }

  @Test
  void testFormatHuman() {
    assertEquals("7d", RelativeDurationParser.formatHuman(Duration.ofDays(7)));
    assertEquals(
        "2d 3h 4m 5s",
        RelativeDurationParser.formatHuman(
            Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(5)));
    assertEquals("0s", RelativeDurationParser.formatHuman(Duration.ZERO));
    assertEquals("0s", RelativeDurationParser.formatHuman(Duration.ofSeconds(-10)));
  }
}
