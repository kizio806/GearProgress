package io.github.kizio806.gearprogress.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationResultTest {

  @Test
  void testValid() {
    ValidationResult valid = ValidationResult.valid();
    assertTrue(valid.isValid());
    assertFalse(valid.hasErrors());
    assertTrue(valid.errors().isEmpty());
    assertTrue(valid.warnings().isEmpty());
  }

  @Test
  void testDiagnostics() {
    ValidationDiagnostic info = ValidationDiagnostic.info("path.a", "Info msg");
    ValidationDiagnostic warning = ValidationDiagnostic.warning("path.b", "Warning msg");
    ValidationDiagnostic error = ValidationDiagnostic.error("path.c", "Error msg");

    ValidationResult result = new ValidationResult(List.of(info, warning, error));
    assertFalse(result.isValid());
    assertTrue(result.hasErrors());
    assertEquals(1, result.errors().size());
    assertEquals(1, result.warnings().size());
    assertEquals("path.c", result.errors().getFirst().path());
  }
}
