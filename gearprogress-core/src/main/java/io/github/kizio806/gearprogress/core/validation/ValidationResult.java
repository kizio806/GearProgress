package io.github.kizio806.gearprogress.core.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a validation pass containing collected diagnostics.
 *
 * @param diagnostics immutable list of diagnostics
 */
public record ValidationResult(List<ValidationDiagnostic> diagnostics) {

  private static final ValidationResult VALID = new ValidationResult(Collections.emptyList());

  /** Constructs a validation result. */
  public ValidationResult {
    Objects.requireNonNull(diagnostics, "diagnostics must not be null");
    diagnostics = List.copyOf(diagnostics);
  }

  /**
   * Returns a successful validation result with no diagnostics.
   *
   * @return valid result
   */
  public static ValidationResult valid() {
    return VALID;
  }

  /**
   * Returns true if there are no error-level diagnostics.
   *
   * @return true if valid
   */
  public boolean isValid() {
    for (ValidationDiagnostic diagnostic : diagnostics) {
      if (diagnostic.severity().isError()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if there is at least one error diagnostic.
   *
   * @return true if invalid
   */
  public boolean hasErrors() {
    return !isValid();
  }

  /**
   * Returns all error-level diagnostics.
   *
   * @return list of error diagnostics
   */
  public List<ValidationDiagnostic> errors() {
    return diagnostics.stream().filter(d -> d.severity().isError()).toList();
  }

  /**
   * Returns all warning-level diagnostics.
   *
   * @return list of warning diagnostics
   */
  public List<ValidationDiagnostic> warnings() {
    return diagnostics.stream().filter(d -> d.severity() == DiagnosticSeverity.WARNING).toList();
  }
}
