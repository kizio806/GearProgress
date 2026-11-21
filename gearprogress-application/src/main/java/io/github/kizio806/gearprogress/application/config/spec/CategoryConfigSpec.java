package io.github.kizio806.gearprogress.application.config.spec;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Raw configuration specification for a category containing tiers.
 *
 * @param tiers map of tier name to tier specification
 */
public record CategoryConfigSpec(Map<String, TierConfigSpec> tiers) {

  /** Constructs an immutable category spec. */
  public CategoryConfigSpec {
    Objects.requireNonNull(tiers, "tiers must not be null");
    tiers = Collections.unmodifiableMap(tiers);
  }
}
