package io.github.kizio806.gearprogress.application.config.compiler;

import io.github.kizio806.gearprogress.application.config.spec.CategoryConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.EquipmentConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TierConfigSpec;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.time.RelativeDurationParser;
import io.github.kizio806.gearprogress.core.validation.ValidationDiagnostic;
import io.github.kizio806.gearprogress.core.validation.ValidationResult;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Compiles raw equipment specifications into validated immutable {@link EquipmentDefinition}s. */
public final class EquipmentDefinitionCompiler {

  /**
   * Compilation result holding successfully compiled definitions and all collected diagnostics.
   *
   * @param definitions compiled definitions
   * @param validationResult validation diagnostics
   */
  public record CompilationResult(
      List<EquipmentDefinition> definitions, ValidationResult validationResult) {

    public CompilationResult {
      Objects.requireNonNull(definitions, "definitions must not be null");
      Objects.requireNonNull(validationResult, "validationResult must not be null");
      definitions = List.copyOf(definitions);
    }

    public boolean isSuccessful() {
      return validationResult.isValid();
    }
  }

  /**
   * Compiles an entire {@link EquipmentConfigSpec} into equipment definitions.
   *
   * @param spec raw equipment spec
   * @return compilation result
   */
  public CompilationResult compile(EquipmentConfigSpec spec) {
    Objects.requireNonNull(spec, "spec must not be null");
    List<EquipmentDefinition> definitions = new ArrayList<>();
    List<ValidationDiagnostic> diagnostics = new ArrayList<>();

    for (Map.Entry<String, CategoryConfigSpec> catEntry : spec.categories().entrySet()) {
      String catName = catEntry.getKey();
      Optional<EquipmentCategory> categoryOpt = EquipmentCategory.fromString(catName);
      if (categoryOpt.isEmpty()) {
        diagnostics.add(
            ValidationDiagnostic.error(
                "equipment." + catName,
                "Unknown equipment category: '"
                    + catName
                    + "'. Valid categories: "
                    + List.of(EquipmentCategory.values())));
        continue;
      }
      EquipmentCategory category = categoryOpt.get();

      for (Map.Entry<String, TierConfigSpec> tierEntry : catEntry.getValue().tiers().entrySet()) {
        String tierName = tierEntry.getKey();
        TierConfigSpec tierSpec = tierEntry.getValue();
        String path = "equipment." + catName + "." + tierName;

        TierKey tierKey;
        try {
          tierKey = TierKey.of(category, tierName);
        } catch (Exception ex) {
          diagnostics.add(ValidationDiagnostic.error(path, "Invalid tier key: " + ex.getMessage()));
          continue;
        }

        UnlockCondition condition = compileUnlockCondition(path, tierSpec, diagnostics);
        RestrictionPolicy restrictionPolicy = compileRestrictionPolicy(path, tierSpec, diagnostics);
        EnchantmentPolicy enchantmentPolicy = EnchantmentPolicy.of(tierSpec.allowEnchantments());

        if (condition != null) {
          definitions.add(
              new EquipmentDefinition(tierKey, condition, restrictionPolicy, enchantmentPolicy));
        }
      }
    }

    ValidationResult validationResult = new ValidationResult(diagnostics);
    return new CompilationResult(definitions, validationResult);
  }

  private UnlockCondition compileUnlockCondition(
      String path, TierConfigSpec spec, List<ValidationDiagnostic> diagnostics) {
    String methodRaw = spec.unlockMethodType().trim().toLowerCase(java.util.Locale.ROOT);
    return switch (methodRaw) {
      case "always" -> UnlockCondition.always();
      case "never" -> UnlockCondition.never();
      case "command" -> UnlockCondition.command();
      case "timer" -> {
        if (spec.timerAfter().isEmpty()) {
          diagnostics.add(
              ValidationDiagnostic.error(
                  path + ".unlock-method",
                  "Timer unlock method requires an 'after' duration property (e.g., after: 7d)"));
          yield null;
        }
        try {
          Duration delay = RelativeDurationParser.parse(spec.timerAfter().get());
          yield UnlockCondition.timer(delay);
        } catch (Exception ex) {
          diagnostics.add(
              ValidationDiagnostic.error(
                  path + ".unlock-method.after",
                  "Invalid duration format '" + spec.timerAfter().get() + "': " + ex.getMessage()));
          yield null;
        }
      }
      case "date" -> {
        if (spec.dateValue().isEmpty()) {
          diagnostics.add(
              ValidationDiagnostic.error(
                  path + ".unlock-method",
                  "Date unlock method requires a 'value' timestamp property (e.g., value: 2026-12-10T18:00:00Z)"));
          yield null;
        }
        try {
          String dateText = spec.dateValue().get();
          Instant target;
          if (!dateText.endsWith("Z") && !dateText.contains("+") && dateText.indexOf('-', 10) < 0) {
            target = Instant.parse(dateText + "Z");
          } else {
            target = Instant.parse(dateText);
          }
          yield UnlockCondition.date(target);
        } catch (Exception ex) {
          diagnostics.add(
              ValidationDiagnostic.error(
                  path + ".unlock-method.value",
                  "Invalid ISO-8601 date timestamp '"
                      + spec.dateValue().get()
                      + "': "
                      + ex.getMessage()));
          yield null;
        }
      }
      default -> {
        diagnostics.add(
            ValidationDiagnostic.error(
                path + ".unlock-method",
                "Unknown unlock method: '"
                    + spec.unlockMethodType()
                    + "'. Supported methods: always, never, timer, date, command"));
        yield null;
      }
    };
  }

  private RestrictionPolicy compileRestrictionPolicy(
      String path, TierConfigSpec spec, List<ValidationDiagnostic> diagnostics) {
    if (spec.restrictions().isEmpty()) {
      return RestrictionPolicy.none();
    }
    EnumSet<RestrictionType> active = EnumSet.noneOf(RestrictionType.class);
    for (Map.Entry<String, Boolean> entry : spec.restrictions().entrySet()) {
      Optional<RestrictionType> typeOpt = RestrictionType.fromString(entry.getKey());
      if (typeOpt.isEmpty()) {
        diagnostics.add(
            ValidationDiagnostic.warning(
                path + ".restrictions." + entry.getKey(),
                "Unknown restriction action '"
                    + entry.getKey()
                    + "'. Allowed: craft, use, equip, pickup, inventory-move"));
        continue;
      }
      if (Boolean.TRUE.equals(entry.getValue())) {
        active.add(typeOpt.get());
      }
    }
    return new RestrictionPolicy(active);
  }
}
