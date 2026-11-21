package io.github.kizio806.gearprogress.application.config.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Config Spec Unit Tests")
class ConfigSpecTest {

  @Test
  @DisplayName("StorageConfigSpec validates nulls and non-positive defaults")
  void testStorageConfigSpecDefaults() {
    assertThrows(NullPointerException.class, () -> new StorageConfigSpec(null, "file.db", 1, 1000));
    assertThrows(NullPointerException.class, () -> new StorageConfigSpec("sqlite", null, 1, 1000));

    StorageConfigSpec fallback = new StorageConfigSpec("sqlite", "file.db", 0, 0);
    assertEquals(1, fallback.maxPoolSize());
    assertEquals(5000, fallback.connectionTimeoutMs());

    StorageConfigSpec def = StorageConfigSpec.defaultSqlite();
    assertEquals("sqlite", def.type());
    assertEquals("data/gearprogress.db", def.file());
    assertEquals(1, def.maxPoolSize());
    assertEquals(5000, def.connectionTimeoutMs());
  }

  @Test
  @DisplayName("TimersConfigSpec validates nulls and creates initial defaults")
  void testTimersConfigSpecDefaults() {
    assertThrows(NullPointerException.class, () -> new TimersConfigSpec(null, null, null));

    Instant now = Instant.now();
    TimersConfigSpec spec = new TimersConfigSpec(now, null, null);
    assertEquals(now, spec.serverStartEpoch());
    assertTrue(spec.pausedStates().isEmpty());
    assertTrue(spec.accumulatedSeconds().isEmpty());

    TimersConfigSpec init = TimersConfigSpec.initial(now);
    assertNotNull(init);
    assertEquals(now, init.serverStartEpoch());
    assertTrue(init.pausedStates().isEmpty());

    TimersConfigSpec withMaps =
        new TimersConfigSpec(now, Map.of("armor:netherite", true), Map.of("armor:netherite", 120L));
    assertEquals(1, withMaps.pausedStates().size());
    assertEquals(120L, withMaps.accumulatedSeconds().get("armor:netherite"));
  }
}
