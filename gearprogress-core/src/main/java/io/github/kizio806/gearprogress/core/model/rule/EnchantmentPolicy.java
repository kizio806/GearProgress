package io.github.kizio806.gearprogress.core.model.rule;

/**
 * Immutable policy governing whether enchantments are allowed on equipment belonging to this tier.
 *
 * @param allowEnchantments whether enchantment application and use are permitted
 */
public record EnchantmentPolicy(boolean allowEnchantments) {

  private static final EnchantmentPolicy ALLOWED = new EnchantmentPolicy(true);
  private static final EnchantmentPolicy FORBIDDEN = new EnchantmentPolicy(false);

  /**
   * Policy allowing enchantments.
   *
   * @return policy with allowEnchantments true
   */
  public static EnchantmentPolicy allowed() {
    return ALLOWED;
  }

  /**
   * Policy forbidding enchantments.
   *
   * @return policy with allowEnchantments false
   */
  public static EnchantmentPolicy forbidden() {
    return FORBIDDEN;
  }

  /**
   * Creates an enchantment policy with the specified boolean flag.
   *
   * @param allow whether enchantments are allowed
   * @return the policy instance
   */
  public static EnchantmentPolicy of(boolean allow) {
    return allow ? ALLOWED : FORBIDDEN;
  }
}
