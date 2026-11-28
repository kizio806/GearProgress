package io.github.kizio806.gearprogress.application.service;

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
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestrictionQueryServiceTest {

  private EquipmentRegistry registry;
  private ProgressionService progressionService;
  private RestrictionQueryService queryService;

  @BeforeEach
  void setUp() {
    registry = new EquipmentRegistry();
    InMemoryProgressionRepository repo = new InMemoryProgressionRepository();
    EventPublisherPort dummyPublisher =
        new EventPublisherPort() {
          @Override
          public void publishUnlock(TierKey key, UnlockMethod method, String source) {}

          @Override
          public void publishLock(TierKey key, String source) {}

          @Override
          public void publishEnchantmentAllowed(TierKey key, boolean allow, String source) {}
        };
    TimePort timePort = () -> Instant.parse("2026-11-20T12:00:00Z");

    progressionService = new ProgressionService(registry, repo, dummyPublisher, timePort);
    queryService = new RestrictionQueryService(progressionService, registry);
  }

  @Test
  void testRestrictionChecks() {
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    RestrictionPolicy policy = RestrictionPolicy.of(RestrictionType.CRAFT, RestrictionType.EQUIP);
    registry.register(
        new EquipmentDefinition(
            diamond, UnlockCondition.command(), policy, EnchantmentPolicy.forbidden()));

    progressionService.initialize().join();

    assertTrue(queryService.isActionRestricted(diamond, RestrictionType.CRAFT));
    assertTrue(queryService.isActionRestricted(diamond, RestrictionType.EQUIP));
    assertFalse(queryService.isActionRestricted(diamond, RestrictionType.USE));
    assertFalse(queryService.isActionRestricted(diamond, RestrictionType.PICKUP));
    assertTrue(queryService.isEnchantmentForbidden(diamond));

    progressionService.unlock(diamond, UnlockMethod.COMMAND, "ADMIN").join();

    assertFalse(queryService.isActionRestricted(diamond, RestrictionType.CRAFT));
    assertFalse(queryService.isActionRestricted(diamond, RestrictionType.EQUIP));
  }
}
