package io.github.kizio806.gearprogress.platform.bukkit.placeholder;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion exposing GearProgress progression metrics, countdowns, and tier
 * statuses.
 */
public final class GearProgressPlaceholderExpansion extends PlaceholderExpansion {

  private final GearProgressApplication application;

  public GearProgressPlaceholderExpansion(GearProgressApplication application) {
    this.application = Objects.requireNonNull(application, "application must not be null");
  }

  @Override
  public @NotNull String getIdentifier() {
    return "gearprogress";
  }

  @Override
  public @NotNull String getAuthor() {
    return "kizio806";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.0.0";
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
    String lower = params.toLowerCase(java.util.Locale.ROOT);

    // %gearprogress_diamond_unlocked%
    if (lower.equals("diamond_unlocked")) {
      boolean unlocked =
          application
              .progressionService()
              .isUnlocked(TierKey.of(EquipmentCategory.ARMOR, "diamond"));
      return String.valueOf(unlocked);
    }

    // %gearprogress_netherite_unlocked%
    if (lower.equals("netherite_unlocked")) {
      boolean unlocked =
          application
              .progressionService()
              .isUnlocked(TierKey.of(EquipmentCategory.ARMOR, "netherite"));
      return String.valueOf(unlocked);
    }

    // %gearprogress_current_stage%
    if (lower.equals("current_stage")) {
      return resolveCurrentStage();
    }

    // %gearprogress_next_unlock%
    if (lower.equals("next_unlock")) {
      return resolveNextUnlock();
    }

    // %gearprogress_time_remaining_<category>_<tier>%
    if (lower.startsWith("time_remaining_")) {
      String remainder = lower.substring("time_remaining_".length());
      return resolveTimeRemaining(remainder);
    }

    // %gearprogress_is_unlocked_<category>_<tier>%
    if (lower.startsWith("is_unlocked_")) {
      String remainder = lower.substring("is_unlocked_".length());
      return resolveIsUnlocked(remainder);
    }

    // %gearprogress_status_<category>_<tier>%
    if (lower.startsWith("status_")) {
      String remainder = lower.substring("status_".length());
      return resolveStatus(remainder);
    }

    return null;
  }

  private String resolveCurrentStage() {
    // Return highest unlocked tier in Armor
    String[] armorTiers = {"netherite", "diamond", "iron", "chainmail", "golden", "leather"};
    for (String tier : armorTiers) {
      if (application.progressionService().isUnlocked(TierKey.of(EquipmentCategory.ARMOR, tier))) {
        return tier;
      }
    }
    return "none";
  }

  private String resolveNextUnlock() {
    Optional<EquipmentDefinition> nextDef =
        application.equipmentRegistry().all().values().stream()
            .filter(def -> !application.progressionService().isUnlocked(def.key()))
            .filter(def -> application.timerService().timeRemaining(def.key()).isPresent())
            .min(
                Comparator.comparing(
                    def -> application.timerService().timeRemaining(def.key()).get().remaining()));

    if (nextDef.isEmpty()) {
      return "none";
    }

    TierKey key = nextDef.get().key();
    TimeRemaining remaining = application.timerService().timeRemaining(key).orElseThrow();
    return key.category().id() + " " + key.tier() + " (" + remaining.formatHuman() + ")";
  }

  private String resolveTimeRemaining(String input) {
    Optional<TierKey> keyOpt = parseTierKeyFromParams(input);
    if (keyOpt.isEmpty()) {
      return "invalid";
    }

    TierKey key = keyOpt.get();
    if (application.progressionService().isUnlocked(key)) {
      return "unlocked";
    }

    Optional<TimeRemaining> remainingOpt = application.timerService().timeRemaining(key);
    return remainingOpt.map(TimeRemaining::formatHuman).orElse("none");
  }

  private String resolveIsUnlocked(String input) {
    Optional<TierKey> keyOpt = parseTierKeyFromParams(input);
    if (keyOpt.isEmpty()) {
      return "false";
    }
    return String.valueOf(application.progressionService().isUnlocked(keyOpt.get()));
  }

  private String resolveStatus(String input) {
    Optional<TierKey> keyOpt = parseTierKeyFromParams(input);
    if (keyOpt.isEmpty()) {
      return "UNKNOWN";
    }
    return application.progressionService().isUnlocked(keyOpt.get()) ? "UNLOCKED" : "LOCKED";
  }

  private Optional<TierKey> parseTierKeyFromParams(String input) {
    int splitIdx = input.indexOf('_');
    if (splitIdx < 0 || splitIdx == input.length() - 1) {
      return Optional.empty();
    }
    String categoryStr = input.substring(0, splitIdx);
    String tierStr = input.substring(splitIdx + 1);

    Optional<EquipmentCategory> categoryOpt = EquipmentCategory.fromString(categoryStr);
    if (categoryOpt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(TierKey.of(categoryOpt.get(), tierStr));
  }
}
