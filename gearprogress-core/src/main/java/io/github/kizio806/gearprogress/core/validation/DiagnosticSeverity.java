package io.github.kizio806.gearprogress.core.validation;

/** Severity level of a configuration or validation diagnostic message. */
public enum DiagnosticSeverity {
  INFO,
  WARNING,
  ERROR;

  /**
   * Checks whether this severity indicates a fatal failure.
   *
   * @return true if error
   */
  public boolean isError() {
    return this == ERROR;
  }
}
