package io.github.kizio806.gearprogress.application.config.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.application.config.spec.EquipmentConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.PluginConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.StorageConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TimersConfigSpec;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class YamlConfigLoaderTest {

  private final YamlConfigLoader loader = new YamlConfigLoader();

  @Test
  void testParsePluginSpec() {
    String yaml =
        """
        locale: "pl-PL"
        timer-check-interval-ticks: 40
        notifications:
          broadcast-unlocks: false
          action-bar-on-blocked: true
          chat-message-on-blocked: false
          blocked-sound: "block.anvil.land"
        bypass:
          enable-permission-bypass: true
          permission: "custom.bypass"
        """;

    Map<String, Object> map = loader.parseMap(yaml);
    PluginConfigSpec spec = loader.parsePluginSpec(map);

    assertEquals("pl-PL", spec.locale());
    assertEquals(40L, spec.timerCheckIntervalTicks());
    assertFalse(spec.broadcastUnlocks());
    assertTrue(spec.actionBarOnBlocked());
    assertFalse(spec.chatMessageOnBlocked());
    assertEquals("block.anvil.land", spec.blockedSound());
    assertTrue(spec.enableBypass());
    assertEquals("custom.bypass", spec.bypassPermission());
  }

  @Test
  void testParseEquipmentSpec() {
    String yaml =
        """
        armor:
          iron:
            unlock-method: always
            allow-enchantments: false
          diamond:
            unlock-method:
              type: timer
              after: "7d"
            allow-enchantments: true
            restrictions:
              craft: true
              equip: true
        """;

    Map<String, Object> map = loader.parseMap(yaml);
    EquipmentConfigSpec spec = loader.parseEquipmentSpec(map);

    assertTrue(spec.categories().containsKey("armor"));
    var armorTiers = spec.categories().get("armor").tiers();
    assertEquals(2, armorTiers.size());
    assertTrue(armorTiers.containsKey("iron"));
    assertTrue(armorTiers.containsKey("diamond"));

    var diamondSpec = armorTiers.get("diamond");
    assertEquals("timer", diamondSpec.unlockMethodType());
    assertEquals("7d", diamondSpec.timerAfter().orElseThrow());
    assertTrue(diamondSpec.allowEnchantments());
    assertTrue(diamondSpec.restrictions().get("craft"));
    assertTrue(diamondSpec.restrictions().get("equip"));
  }

  @Test
  void testParseTimersSpec() {
    String yaml =
        """
        server-start-epoch: "2026-11-20T10:00:00Z"
        timers:
          armor:diamond:
            paused: true
            accumulated-seconds: 3600
        """;

    Map<String, Object> map = loader.parseMap(yaml);
    TimersConfigSpec spec = loader.parseTimersSpec(map, Instant.now());

    assertEquals(Instant.parse("2026-11-20T10:00:00Z"), spec.serverStartEpoch());
    assertTrue(spec.pausedStates().get("armor:diamond"));
    assertEquals(3600L, spec.accumulatedSeconds().get("armor:diamond"));
  }

  @Test
  void testParseStorageSpec() {
    String yaml =
        """
        storage:
          type: "sqlite"
          file: "custom/db.sqlite"
          pool:
            maximum-pool-size: 2
            connection-timeout-ms: 8000
        """;

    Map<String, Object> map = loader.parseMap(yaml);
    StorageConfigSpec spec = loader.parseStorageSpec(map);

    assertEquals("sqlite", spec.type());
    assertEquals("custom/db.sqlite", spec.file());
    assertEquals(2, spec.maxPoolSize());
    assertEquals(8000L, spec.connectionTimeoutMs());
  }

  @Test
  void testYamlLoaderEdgeCasesAndFallbacks() throws Exception {
    assertTrue(loader.parseMap("").isEmpty());
    assertTrue(loader.parseMap(null).isEmpty());
    assertTrue(loader.parseMap("- item1\n- item2").isEmpty());

    assertTrue(loader.loadFile(java.nio.file.Path.of("non_existent_file.yml")).isEmpty());

    Instant now = Instant.now();
    TimersConfigSpec fallbackSpec =
        loader.parseTimersSpec(Map.of("server-start-epoch", "not-a-date"), now);
    assertEquals(now, fallbackSpec.serverStartEpoch());

    StorageConfigSpec defaultStorage = loader.parseStorageSpec(Map.of());
    assertEquals("sqlite", defaultStorage.type());
    assertEquals("data/gearprogress.db", defaultStorage.file());
  }
}
