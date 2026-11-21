package io.github.kizio806.gearprogress.application.port;

/** Outbound SPI port for application lifecycle reporting and logging. */
public interface LifecycleReporterPort {

  /**
   * Logs an informational message.
   *
   * @param message text
   */
  void info(String message);

  /**
   * Logs a warning message.
   *
   * @param message text
   */
  void warning(String message);

  /**
   * Logs a severe error with an optional cause.
   *
   * @param message text
   * @param throwable cause or null
   */
  void severe(String message, Throwable throwable);
}
