package io.github.kizio806.gearprogress.application.port;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Outbound SPI port for time queries. Provides a clock abstraction enabling deterministic unit
 * testing.
 */
@FunctionalInterface
public interface TimePort {

  /**
   * Returns the current instant.
   *
   * @return now
   */
  Instant now();

  /**
   * Returns system UTC time port.
   *
   * @return system time port
   */
  static TimePort systemUtc() {
    return Instant::now;
  }

  /**
   * Returns a time port backed by a specific clock.
   *
   * @param clock the clock
   * @return time port
   */
  static TimePort of(Clock clock) {
    return clock::instant;
  }

  /**
   * Returns the default zone for this time port (UTC).
   *
   * @return ZoneId
   */
  default ZoneId zone() {
    return ZoneId.of("UTC");
  }
}
