package io.github.kizio806.gearprogress.application.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TimePortTest {

  @Test
  void testSystemUtc() {
    TimePort port = TimePort.systemUtc();
    assertNotNull(port.now());
    assertEquals(ZoneId.of("UTC"), port.zone());
  }

  @Test
  void testClockBacked() {
    Instant fixed = Instant.parse("2026-11-20T12:00:00Z");
    Clock clock = Clock.fixed(fixed, ZoneId.of("UTC"));
    TimePort port = TimePort.of(clock);

    assertEquals(fixed, port.now());
    assertEquals(ZoneId.of("UTC"), port.zone());
  }
}
