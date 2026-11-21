package io.github.kizio806.gearprogress.core.model.unlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UnlockConditionTest {

  @Test
  void testAlwaysCondition() {
    UnlockCondition.Always always = UnlockCondition.always();
    assertEquals(UnlockMethod.ALWAYS, always.method());
  }

  @Test
  void testNeverCondition() {
    UnlockCondition.Never never = UnlockCondition.never();
    assertEquals(UnlockMethod.NEVER, never.method());
  }

  @Test
  void testTimerCondition() {
    Duration delay = Duration.ofDays(7);
    UnlockCondition.Timer timer = UnlockCondition.timer(delay);
    assertEquals(UnlockMethod.TIMER, timer.method());
    assertEquals(delay, timer.delay());

    assertThrows(NullPointerException.class, () -> UnlockCondition.timer(null));
    assertThrows(IllegalArgumentException.class, () -> UnlockCondition.timer(Duration.ZERO));
    assertThrows(
        IllegalArgumentException.class, () -> UnlockCondition.timer(Duration.ofSeconds(-10)));
  }

  @Test
  void testDateCondition() {
    Instant target = Instant.parse("2026-12-10T18:00:00Z");
    UnlockCondition.Date date = UnlockCondition.date(target);
    assertEquals(UnlockMethod.DATE, date.method());
    assertEquals(target, date.targetDate());

    assertThrows(NullPointerException.class, () -> UnlockCondition.date(null));
  }

  @Test
  void testCommandCondition() {
    UnlockCondition.Command command = UnlockCondition.command();
    assertEquals(UnlockMethod.COMMAND, command.method());
  }

  @Test
  void testUnlockMethodParsing() {
    assertEquals(UnlockMethod.TIMER, UnlockMethod.fromString("timer").orElseThrow());
    assertEquals(UnlockMethod.COMMAND, UnlockMethod.fromString("COMMAND").orElseThrow());
    assertEquals(UnlockMethod.DATE, UnlockMethod.fromStringOrThrow("date"));
    assertFalse(UnlockMethod.fromString("unknown").isPresent());
    assertThrows(IllegalArgumentException.class, () -> UnlockMethod.fromStringOrThrow("bad"));
    assertThrows(NullPointerException.class, () -> UnlockMethod.fromStringOrThrow(null));
    assertEquals("always", UnlockMethod.ALWAYS.id());
  }
}
