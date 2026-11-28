package io.github.kizio806.gearprogress.application.config.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.application.config.spec.CategoryConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.EquipmentConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TierConfigSpec;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EquipmentDefinitionCompilerTest {

  private final EquipmentDefinitionCompiler compiler = new EquipmentDefinitionCompiler();

  @Test
  void testCompileValidTiers() {
    TierConfigSpec ironSpec =
        new TierConfigSpec(
            "always",
            Optional.empty(),
            Optional.empty(),
            true,
            Map.of("craft", false, "use", false));

    TierConfigSpec diamondSpec =
        new TierConfigSpec(
            "timer",
            Optional.of("7d"),
            Optional.empty(),
            false,
            Map.of("craft", true, "equip", true));

    CategoryConfigSpec armorSpec =
        new CategoryConfigSpec(
            Map.of(
                "iron", ironSpec,
                "diamond", diamondSpec));

    EquipmentConfigSpec rootSpec = new EquipmentConfigSpec(Map.of("armor", armorSpec));

    var result = compiler.compile(rootSpec);
    assertTrue(result.isSuccessful());
    assertEquals(2, result.definitions().size());

    var ironDef =
        result.definitions().stream()
            .filter(d -> d.key().equals(TierKey.of(EquipmentCategory.ARMOR, "iron")))
            .findFirst()
            .orElseThrow();

    assertTrue(ironDef.unlockCondition() instanceof UnlockCondition.Always);
    assertTrue(ironDef.enchantmentPolicy().allowEnchantments());
    assertFalse(ironDef.restrictionPolicy().hasAnyRestrictions());

    var diamondDef =
        result.definitions().stream()
            .filter(d -> d.key().equals(TierKey.of(EquipmentCategory.ARMOR, "diamond")))
            .findFirst()
            .orElseThrow();

    assertTrue(diamondDef.unlockCondition() instanceof UnlockCondition.Timer);
    assertFalse(diamondDef.enchantmentPolicy().allowEnchantments());
    assertTrue(diamondDef.restrictionPolicy().isRestricted(RestrictionType.CRAFT));
    assertTrue(diamondDef.restrictionPolicy().isRestricted(RestrictionType.EQUIP));
    assertFalse(diamondDef.restrictionPolicy().isRestricted(RestrictionType.USE));
  }

  @Test
  void testCompileInvalidCategory() {
    TierConfigSpec spec =
        new TierConfigSpec("always", Optional.empty(), Optional.empty(), true, Map.of());
    CategoryConfigSpec cat = new CategoryConfigSpec(Map.of("iron", spec));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("vehicles", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful());
    assertTrue(result.validationResult().hasErrors());
  }

  @Test
  void testCompileInvalidTimerWithoutAfter() {
    TierConfigSpec spec =
        new TierConfigSpec("timer", Optional.empty(), Optional.empty(), true, Map.of());
    CategoryConfigSpec cat = new CategoryConfigSpec(Map.of("iron", spec));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("armor", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful());
  }

  @Test
  void testCompileDateUnlockMethod() {
    TierConfigSpec dateSpecWithZ =
        new TierConfigSpec(
            "date", Optional.empty(), Optional.of("2026-12-10T18:00:00Z"), true, Map.of());
    TierConfigSpec dateSpecWithoutZ =
        new TierConfigSpec(
            "date", Optional.empty(), Optional.of("2026-12-10T18:00:00"), true, Map.of());
    TierConfigSpec dateSpecMissing =
        new TierConfigSpec("date", Optional.empty(), Optional.empty(), true, Map.of());
    TierConfigSpec dateSpecInvalid =
        new TierConfigSpec("date", Optional.empty(), Optional.of("invalid-date"), true, Map.of());

    CategoryConfigSpec cat =
        new CategoryConfigSpec(
            Map.of(
                "leather", dateSpecWithZ,
                "iron", dateSpecWithoutZ,
                "diamond", dateSpecMissing,
                "netherite", dateSpecInvalid));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("armor", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful());
    assertEquals(2, result.definitions().size());
  }

  @Test
  void testCompileUnknownUnlockMethod() {
    TierConfigSpec spec =
        new TierConfigSpec("magic", Optional.empty(), Optional.empty(), true, Map.of());
    CategoryConfigSpec cat = new CategoryConfigSpec(Map.of("iron", spec));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("armor", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful());
  }

  @Test
  void testCompileInvalidTierKey() {
    TierConfigSpec spec =
        new TierConfigSpec("always", Optional.empty(), Optional.empty(), true, Map.of());
    CategoryConfigSpec cat = new CategoryConfigSpec(Map.of("   ", spec));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("armor", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful());
  }

  @Test
  void testCompileNeverCommandAndRestrictionsWarnings() {
    TierConfigSpec neverSpec =
        new TierConfigSpec("never", Optional.empty(), Optional.empty(), false, Map.of());
    TierConfigSpec cmdSpec =
        new TierConfigSpec(
            "command",
            Optional.empty(),
            Optional.empty(),
            true,
            Map.of("unknown_action", true, "craft", true));
    TierConfigSpec invalidTimer =
        new TierConfigSpec(
            "timer", Optional.of("invalid_duration_string"), Optional.empty(), true, Map.of());

    CategoryConfigSpec cat =
        new CategoryConfigSpec(
            Map.of("leather", neverSpec, "diamond", cmdSpec, "netherite", invalidTimer));
    EquipmentConfigSpec root = new EquipmentConfigSpec(Map.of("armor", cat));

    var result = compiler.compile(root);
    assertFalse(result.isSuccessful()); // invalidTimer causes error
    assertFalse(result.validationResult().warnings().isEmpty()); // unknown_action causes warning

    var definitions = result.definitions();
    assertEquals(2, definitions.size());

    var neverDef =
        definitions.stream()
            .filter(d -> d.key().tier().equals("leather"))
            .findFirst()
            .orElseThrow();
    assertTrue(neverDef.unlockCondition() instanceof UnlockCondition.Never);

    var cmdDef =
        definitions.stream()
            .filter(d -> d.key().tier().equals("diamond"))
            .findFirst()
            .orElseThrow();
    assertTrue(cmdDef.unlockCondition() instanceof UnlockCondition.Command);
    assertTrue(cmdDef.restrictionPolicy().isRestricted(RestrictionType.CRAFT));
  }
}
