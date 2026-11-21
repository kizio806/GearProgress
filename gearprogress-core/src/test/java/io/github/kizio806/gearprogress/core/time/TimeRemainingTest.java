package io.github.kizio806.gearprogress.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimeRemainingTest {

  @Test
  void testUntilFuture() {
    Instant now = Instant.parse("2026-11-20T12:00:00Z");
    Instant target = Instant.parse("2026-11-20T14:30:00Z");

    TimeRemaining remaining = TimeRemaining.until(target, now);
    assertFalse(remaining.isExpired());
    assertEquals(Duration.ofHours(2).plusMinutes(30), remaining.remaining());
    assertEquals("2h 30m", remaining.formatHuman());
  }

  @Test
  void testUntilPastOrSame() {
    Instant now = Instant.parse("2026-11-20T12:00:00Z");
    Instant past = Instant.parse("2026-11-20T11:00:00Z");

    TimeRemaining remainingPast = TimeRemaining.until(past, now);
    assertTrue(remainingPast.isExpired());
    assertEquals(Duration.ZERO, remainingPast.remaining());

    TimeRemaining remainingSame = TimeRemaining.until(now, now);
    assertTrue(remainingSame.isExpired());

    TimeRemaining expired = TimeRemaining.expired();
    assertTrue(expired.isExpired());
  }
}
