package io.github.kizio806.gearprogress.core.validation;

import java.util.Objects;

/**
 * Diagnostic message produced during configuration validation or compiler passes.
 *
 * @param path the property path or identifier associated with the diagnostic
 * @param message human-readable description
 * @param severity the severity level
 */
public record ValidationDiagnostic(String path, String message, DiagnosticSeverity severity) {

  /** Constructs a validated diagnostic. */
  public ValidationDiagnostic {
    Objects.requireNonNull(path, "path must not be null");
    Objects.requireNonNull(message, "message must not be null");
    Objects.requireNonNull(severity, "severity must not be null");
  }

  /**
   * Creates an error diagnostic.
   *
   * @param path configuration path
   * @param message explanation
   * @return diagnostic instance
   */
  public static ValidationDiagnostic error(String path, String message) {
    return new ValidationDiagnostic(path, message, DiagnosticSeverity.ERROR);
  }

  /**
   * Creates a warning diagnostic.
   *
   * @param path configuration path
   * @param message explanation
   * @return diagnostic instance
   */
  public static ValidationDiagnostic warning(String path, String message) {
    return new ValidationDiagnostic(path, message, DiagnosticSeverity.WARNING);
  }

  /**
   * Creates an informational diagnostic.
   *
   * @param path configuration path
   * @param message explanation
   * @return diagnostic instance
   */
  public static ValidationDiagnostic info(String path, String message) {
    return new ValidationDiagnostic(path, message, DiagnosticSeverity.INFO);
  }
}
