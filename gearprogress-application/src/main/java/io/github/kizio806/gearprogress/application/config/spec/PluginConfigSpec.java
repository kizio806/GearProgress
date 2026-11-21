package io.github.kizio806.gearprogress.application.config.spec;

import java.util.Objects;

/**
 * General plugin configuration specification mapped from config.yml.
 *
 * @param locale selected language code (e.g., "en-US", "pl-PL")
 * @param timerCheckIntervalTicks ticks between timer progression evaluation passes
 * @param broadcastUnlocks whether tier unlocks trigger a global server broadcast
 * @param actionBarOnBlocked whether an action bar alert is shown when an action is blocked
 * @param chatMessageOnBlocked whether a chat notification is shown when an action is blocked
 * @param blockedSound Bukkit sound effect identifier or empty
 * @param enableBypass whether the bypass permission is active
 * @param bypassPermission permission string for bypassing progression restrictions
 */
public record PluginConfigSpec(
    String locale,
    long timerCheckIntervalTicks,
    boolean broadcastUnlocks,
    boolean actionBarOnBlocked,
    boolean chatMessageOnBlocked,
    String blockedSound,
    boolean enableBypass,
    String bypassPermission) {

  /** Constructs a validated plugin config spec. */
  public PluginConfigSpec {
    Objects.requireNonNull(locale, "locale must not be null");
    Objects.requireNonNull(blockedSound, "blockedSound must not be null");
    Objects.requireNonNull(bypassPermission, "bypassPermission must not be null");
    if (timerCheckIntervalTicks <= 0) {
      timerCheckIntervalTicks = 20;
    }
  }

  /**
   * Returns default settings.
   *
   * @return default spec
   */
  public static PluginConfigSpec defaults() {
    return new PluginConfigSpec(
        "en-US", 20L, true, true, true, "entity.villager.no", true, "gearprogress.bypass");
  }
}
