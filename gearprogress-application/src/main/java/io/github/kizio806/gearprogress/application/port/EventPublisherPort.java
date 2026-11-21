package io.github.kizio806.gearprogress.application.port;

import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;

/** Outbound SPI port for publishing progression events to the server's event bus. */
public interface EventPublisherPort {

  /**
   * Publishes an equipment tier unlock event.
   *
   * @param key the tier key
   * @param method the method of unlock
   * @param source the operator or trigger name
   */
  void publishUnlock(TierKey key, UnlockMethod method, String source);

  /**
   * Publishes an equipment tier lock event.
   *
   * @param key the tier key
   * @param source the operator or trigger name
   */
  void publishLock(TierKey key, String source);

  /**
   * Publishes an enchantment allowed change event.
   *
   * @param key the tier key
   * @param allow whether enchantments are now allowed
   * @param source the operator or trigger name
   */
  void publishEnchantmentAllowed(TierKey key, boolean allow, String source);
}
