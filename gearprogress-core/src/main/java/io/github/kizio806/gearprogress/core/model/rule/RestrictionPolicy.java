package io.github.kizio806.gearprogress.core.model.rule;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable policy defining which actions are restricted for a tier when it is locked.
 *
 * @param activeRestrictions the set of restricted actions
 */
public record RestrictionPolicy(Set<RestrictionType> activeRestrictions) {

  private static final RestrictionPolicy EMPTY = new RestrictionPolicy(Collections.emptySet());
  private static final RestrictionPolicy ALL =
      new RestrictionPolicy(EnumSet.allOf(RestrictionType.class));

  /**
   * Constructs a validated restriction policy.
   *
   * @param activeRestrictions non-null set of restrictions
   */
  public RestrictionPolicy {
    Objects.requireNonNull(activeRestrictions, "activeRestrictions must not be null");
    activeRestrictions =
        Collections.unmodifiableSet(
            activeRestrictions.isEmpty()
                ? Collections.emptySet()
                : EnumSet.copyOf(activeRestrictions));
  }

  /**
   * Returns a policy with no restrictions active.
   *
   * @return the empty policy
   */
  public static RestrictionPolicy none() {
    return EMPTY;
  }

  /**
   * Returns a policy with all restrictions active.
   *
   * @return the policy blocking all actions
   */
  public static RestrictionPolicy all() {
    return ALL;
  }

  /**
   * Creates a restriction policy with the specified restriction types.
   *
   * @param restrictions the restriction types
   * @return the policy
   */
  public static RestrictionPolicy of(RestrictionType... restrictions) {
    Objects.requireNonNull(restrictions, "restrictions array must not be null");
    if (restrictions.length == 0) {
      return EMPTY;
    }
    EnumSet<RestrictionType> set = EnumSet.noneOf(RestrictionType.class);
    Collections.addAll(set, restrictions);
    return new RestrictionPolicy(set);
  }

  /**
   * Checks whether a specific player action is restricted by this policy.
   *
   * @param type the restriction type to check
   * @return true if the action is blocked, false otherwise
   */
  public boolean isRestricted(RestrictionType type) {
    Objects.requireNonNull(type, "type must not be null");
    return activeRestrictions.contains(type);
  }

  /**
   * Returns true if any action is restricted by this policy.
   *
   * @return true if non-empty
   */
  public boolean hasAnyRestrictions() {
    return !activeRestrictions.isEmpty();
  }
}
