package io.github.kizio806.gearprogress.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.application.repository.InMemoryProgressionRepository;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgressionServiceTest {

  private EquipmentRegistry registry;
  private InMemoryProgressionRepository repository;
  private TestEventPublisher eventPublisher;
  private TimePort timePort;
  private ProgressionService service;

  private final Instant testNow = Instant.parse("2026-11-20T12:00:00Z");

  @BeforeEach
  void setUp() {
    registry = new EquipmentRegistry();
    repository = new InMemoryProgressionRepository();
    eventPublisher = new TestEventPublisher();
    timePort = () -> testNow;

    service = new ProgressionService(registry, repository, eventPublisher, timePort);
  }

  @Test
  void testInitializeAndUnlock() {
    TierKey iron = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    registry.register(
        new EquipmentDefinition(
            iron, UnlockCondition.always(), RestrictionPolicy.none(), EnchantmentPolicy.allowed()));
    registry.register(
        new EquipmentDefinition(
            diamond,
            UnlockCondition.command(),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden()));

    service.initialize().join();

    assertTrue(service.isUnlocked(iron));
    assertFalse(service.isUnlocked(diamond));
    assertTrue(service.isEnchantmentAllowed(iron));
    assertFalse(service.isEnchantmentAllowed(diamond));

    boolean changed = service.unlock(diamond, UnlockMethod.COMMAND, "ADMIN").join();
    assertTrue(changed);
    assertTrue(service.isUnlocked(diamond));
    assertEquals(1, eventPublisher.unlockEvents.size());

    boolean secondChange = service.unlock(diamond, UnlockMethod.COMMAND, "ADMIN").join();
    assertFalse(secondChange);

    boolean lockedChanged = service.lock(diamond, "ADMIN").join();
    assertTrue(lockedChanged);
    assertFalse(service.isUnlocked(diamond));
    assertEquals(1, eventPublisher.lockEvents.size());

    boolean secondLock = service.lock(diamond, "ADMIN").join();
    assertFalse(secondLock);

    TierKey unregisteredNetherite = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    boolean lockedUnregistered = service.lock(unregisteredNetherite, "ADMIN").join();
    assertTrue(lockedUnregistered);
    assertFalse(service.isUnlocked(unregisteredNetherite));
  }

  @Test
  void testReset() {
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    registry.register(
        new EquipmentDefinition(
            diamond,
            UnlockCondition.command(),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden()));

    service.initialize().join();
    service.unlock(diamond, UnlockMethod.COMMAND, "ADMIN").join();
    assertTrue(service.isUnlocked(diamond));

    service.reset(EquipmentCategory.ARMOR, "ADMIN").join();
    assertFalse(service.isUnlocked(diamond));

    service.unlock(diamond, UnlockMethod.COMMAND, "ADMIN").join();
    assertTrue(service.isUnlocked(diamond));
    service.reset(null, "ADMIN").join();
    assertFalse(service.isUnlocked(diamond));

    TierKey leather = TierKey.of(EquipmentCategory.ARMOR, "leather");
    registry.register(
        new EquipmentDefinition(
            leather,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed()));
    service.initialize().join();
    assertTrue(service.isUnlocked(leather));
    service.lock(leather, "ADMIN").join();
    assertFalse(service.isUnlocked(leather));
    service.reset(EquipmentCategory.ARMOR, "ADMIN").join();
    assertTrue(service.isUnlocked(leather));
  }

  @Test
  void testUnlockUnregisteredTier() {
    TierKey unregistered = TierKey.of(EquipmentCategory.TOOLS, "copper");
    boolean unlocked = service.unlock(unregistered, UnlockMethod.COMMAND, "ADMIN").join();
    assertTrue(unlocked);
    assertTrue(service.isUnlocked(unregistered));
  }

  @Test
  void testEnchantmentPermissionsAndState() {
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    registry.register(
        new EquipmentDefinition(
            diamond,
            UnlockCondition.command(),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden()));

    service.initialize().join();
    assertFalse(service.isEnchantmentAllowed(diamond));

    service.setEnchantmentAllowed(diamond, true, "ADMIN").join();
    assertTrue(service.isEnchantmentAllowed(diamond));
    assertEquals(1, eventPublisher.enchantEvents.size());

    service.setEnchantmentAllowed(diamond, true, "ADMIN").join();
    assertTrue(service.isEnchantmentAllowed(diamond));

    TierKey unregisteredTool = TierKey.of(EquipmentCategory.TOOLS, "copper");
    service.setEnchantmentAllowed(unregisteredTool, false, "ADMIN").join();
    assertTrue(service.isEnchantmentAllowed(unregisteredTool));

    assertTrue(service.state(diamond).isPresent());
    assertEquals(1, service.snapshot().states().size());

    TierKey unregistered = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    assertTrue(service.isUnlocked(unregistered));
    assertTrue(service.isEnchantmentAllowed(unregistered));
    assertTrue(service.state(unregistered).isEmpty());
  }

  private static class TestEventPublisher implements EventPublisherPort {
    final List<TierKey> unlockEvents = new ArrayList<>();
    final List<TierKey> lockEvents = new ArrayList<>();
    final List<TierKey> enchantEvents = new ArrayList<>();

    @Override
    public void publishUnlock(TierKey key, UnlockMethod method, String source) {
      unlockEvents.add(key);
    }

    @Override
    public void publishLock(TierKey key, String source) {
      lockEvents.add(key);
    }

    @Override
    public void publishEnchantmentAllowed(TierKey key, boolean allow, String source) {
      enchantEvents.add(key);
    }
  }
}
